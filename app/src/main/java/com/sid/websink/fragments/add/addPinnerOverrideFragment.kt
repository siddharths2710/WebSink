package com.sid.websink.fragments.add

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sid.websink.DomainHandler
import com.sid.websink.R
import com.sid.websink.data.PinnerMapping
import com.sid.websink.data.PinnerViewModel
import com.sid.websink.fragments.list.ListDomainOverrideFragment
import com.sid.websink.fragments.list.ListPinnerOverrideFragment
import kotlinx.android.synthetic.main.fragment_add_pinner_override.view.*
import org.w3c.dom.Text
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest


class addPinnerOverrideFragment : Fragment() {

    private enum class TAG(val id: Int) {
        CREATE_VIEW(86),
        CERT_INTENT(87),
        INSERT_DB(88),
        READ_EXT_STORAGE(89),
        CERT_READ_ERR(91)
    }
    private lateinit var  mPinnerViewModel: PinnerViewModel
    private var domainEntry: String? = null
    private var certHash: String? = null
    private val hashType: String = "sha256"

    private lateinit var domainHandler: DomainHandler
    private var certHashTextView: TextView? = null
    private val getCert = registerForActivityResult(ActivityResultContracts.GetContent()) {uri: Uri? ->

        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), TAG.READ_EXT_STORAGE as Int)
        }
        try {
            //Obtain SHA256 fingerprint of locally-stored X.509 cert
            if(uri?.path != null) {
                val certFile = RandomAccessFile(uri?.path!!.split(":").last(), "r")
                val certContents = ByteArray(certFile.length().toInt())
                certFile.readFully(certContents)
                val md = MessageDigest.getInstance("SHA-256")
                md.update(certContents)
                certHash = String(Base64.encode(md.digest(), 0))
                certHashTextView?.text = "$hashType/$certHash"
            }
        } catch (ex:Exception) {
            if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Please provide read permission for app and restart app", Toast.LENGTH_LONG)
            } else {
                ex.message?.let { Log.w(TAG.CERT_READ_ERR.toString(), it) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        domainHandler = DomainHandler.getDomainHandler(requireActivity().applicationContext)
        activity?.actionBar?.title = "Add Cert Pinner Mapping"
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_pinner_override, container, false)
        certHashTextView = view.certHashText as TextView
        mPinnerViewModel = ViewModelProvider(this).get(PinnerViewModel::class.java)

        view.certChooseBtn.setOnClickListener {
            getCert.launch("application/x-x509-ca-cert")
            view.submitPinningCfgBtn.visibility = View.VISIBLE
        }
        view.submitPinningCfgBtn.setOnClickListener {
            domainEntry = view.domainText.text.toString()
            insertContentstoDB()
        }
        return view
    }

    private fun insertContentstoDB() {
        if(!domainHandler.isValidDomain(domainEntry!!)) {
            Toast.makeText(requireContext(), "Please enter valid domain", Toast.LENGTH_SHORT).show()
        } else if (!hashCheck(certHash, hashType)) {
            Toast.makeText(requireContext(), "Please upload valid cert", Toast.LENGTH_SHORT).show()
        } else {
            val pinnerMapping = PinnerMapping(0, domainEntry, hashType, certHash)
            mPinnerViewModel.addMapping(pinnerMapping)
            Toast.makeText(requireContext(), "Added Pinned mapping", Toast.LENGTH_SHORT).show()

            childFragmentManager.popBackStack()
            val listFragment = ListPinnerOverrideFragment()
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.addPinnerOverrideFragmentLayout, listFragment)
            transaction.commitAllowingStateLoss()
        }
    }

    private fun hashCheck(hash: String?, algo: String): Boolean {
        return hash != null && hash.isNotEmpty()  && (algo == "sha1" || algo == "sha256")
    }
}