package com.sid.websink.fragments.add

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sid.websink.R
import com.sid.websink.data.PinnerMapping
import com.sid.websink.data.PinnerViewModel
import com.sid.websink.fragments.list.ListDomainOverrideFragment
import com.sid.websink.fragments.list.ListPinnerOverrideFragment
import kotlinx.android.synthetic.main.fragment_add_pinner_override.view.*
import org.w3c.dom.Text
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest


class addPinnerOverrideFragment : Fragment() {

    private enum class TAG(val id: Int) {
        CREATE_VIEW(86),
        CERT_INTENT(87),
        INSERT_DB(88)
    }
    private lateinit var  mPinnerViewModel: PinnerViewModel
    private var domainEntry: String? = null
    private var certHash: String? = null

    private var certHashTextView: TextView? = null
    private val getCert = registerForActivityResult(ActivityResultContracts.GetContent()) {uri: Uri? ->
        //Obtain SHA256 fingerprint of locally-stored X.509 cert
        if(uri?.path != null) {
            val certFile = RandomAccessFile(uri?.path, "r")
            val certContents = ByteArray(certFile.length() as Int)
            certFile.readFully(certContents)
            val md = MessageDigest.getInstance("SHA-256")
            md.update(certContents)
            certHash = String(Base64.encode(md.digest(), 0))
            certHashTextView?.text = "sha256/$certHash"
        }
    }

    override fun onStart() {
        super.onStart()
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
            insertContentstoDB()
        }
        return view
    }

    private fun insertContentstoDB() {
        if(!domainCheck(domainEntry)) {
            Toast.makeText(requireContext(), "Please enter valid domain", Toast.LENGTH_SHORT).show()
        } else if (!hashCheck(certHash)) {
            Toast.makeText(requireContext(), "Please upload valid cert", Toast.LENGTH_SHORT).show()
        } else {
            val pinnerMapping = PinnerMapping(0, domainEntry, "sha256", certHash)
            mPinnerViewModel.addMapping(pinnerMapping)
            Toast.makeText(requireContext(), "Added Pinned mapping", Toast.LENGTH_SHORT).show()

            //findNavController().navigate(R.id.action_addPinnerOverrideFragment_to_listPinnerOverrideFragment)
            childFragmentManager.popBackStack()
            val listFragment = ListPinnerOverrideFragment()
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.addPinnerOverrideFragmentLayout, listFragment)
            transaction.commitAllowingStateLoss()
        }
    }

    private fun domainCheck(domain: String?):Boolean {
        return domain != null && domain.isNotEmpty()
    }
    private fun hashCheck(hash: String?): Boolean {
        return hash != null && hash.isNotEmpty()
    }
    /*private fun openCertChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/x-x509-ca-cert";
            putExtra(Intent.EXTRA_MIME_TYPES, "application/x-pem-file");
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        }
        try {
            startActivityForResult(intent, TAG.CERT_INTENT)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG.CERT_INTENT + "", "openCertChooser: ", e + "")
        }
    }*/

}