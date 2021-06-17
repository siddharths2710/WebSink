package com.sid.websink.fragments.add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.sid.websink.DomainHandler
import com.sid.websink.R
import com.sid.websink.data.DomainOverrideMapping
import com.sid.websink.data.DomainOverrideViewModel
import kotlinx.android.synthetic.main.fragment_add_domain_override.*
import kotlinx.android.synthetic.main.fragment_add_domain_override.view.*


class addDomainOverrideFragment : Fragment() {

    private lateinit var mDomainOverrideViewModel: DomainOverrideViewModel
    private lateinit var mDomainHandler: DomainHandler

    override fun onStart() {
        super.onStart()
        mDomainHandler = DomainHandler.getDomainHandler(requireContext())
        activity?.actionBar?.title = "Add Domain Mapping"
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
      val view = inflater.inflate(R.layout.fragment_add_domain_override, container, false)
        mDomainOverrideViewModel = ViewModelProvider(this).get(DomainOverrideViewModel::class.java)

        view.blacklistYesBtn.setOnClickListener {
            new_domain_text.setText("")
            insertContentstoDB()
        }

        view.blacklistNoBtn.setOnClickListener {
            view.addDomainBlacklistRow.visibility = View.GONE
            view.new_domain_text.visibility = View.VISIBLE
            view.domain_mapping_btn.visibility = View.VISIBLE
        }

        view.domain_mapping_btn.setOnClickListener {
            insertContentstoDB()
        }
        return view
    }
    private fun insertContentstoDB() {
        val old_domain = old_domain_text.text.toString()
        val new_domain = new_domain_text.text.toString()
        if(mDomainHandler.isValidDomain(old_domain) && ( new_domain.isEmpty() || mDomainHandler.isValidDomain(new_domain))) {
            val domainMapping = DomainOverrideMapping(old_domain, new_domain)
            mDomainOverrideViewModel.addMapping(domainMapping)
            Toast.makeText(requireContext(), "Domain Mappings added to Database", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStackImmediate()

        } else {
            Toast.makeText(requireContext(), "Please add proper domain", Toast.LENGTH_SHORT).show()
        }
    }
}