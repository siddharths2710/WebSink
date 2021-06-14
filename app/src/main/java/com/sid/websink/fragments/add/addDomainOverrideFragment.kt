package com.sid.websink.fragments.add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sid.websink.R
import com.sid.websink.data.DomainOverrideMapping
import com.sid.websink.data.DomainOverrideViewModel
import com.sid.websink.fragments.list.ListDomainOverrideFragment
import kotlinx.android.synthetic.main.fragment_add_domain_override.*
import kotlinx.android.synthetic.main.fragment_add_domain_override.view.*


class addDomainOverrideFragment : Fragment() {

    private lateinit var mDomainOverrideViewModel: DomainOverrideViewModel

    override fun onStart() {
        super.onStart()
        activity?.actionBar?.title = "Add Domain Mapping"
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
      val view = inflater.inflate(R.layout.fragment_add_domain_override, container, false)
        mDomainOverrideViewModel = ViewModelProvider(this).get(DomainOverrideViewModel::class.java)
        view.domain_mapping_btn.setOnClickListener {
            insertContentstoDB()
        }
        return view
    }
    private fun insertContentstoDB() {
        val old_domain = old_domain_text.text.toString()
        val new_domain = new_domain_text.text.toString()
        if(domainCheck(old_domain) && domainCheck(new_domain)) {
            val domainMapping = DomainOverrideMapping(old_domain, new_domain)
            mDomainOverrideViewModel.addMapping(domainMapping)
            Toast.makeText(requireContext(), "Domain Mappings added to Database", Toast.LENGTH_SHORT).show()

            //findNavController().navigate(R.id.action_addDomainOverrideFragment_to_listDomainOverrideFragment)
            childFragmentManager.popBackStack()
            //val listFragment = ListDomainOverrideFragment()
            //val transaction = childFragmentManager.beginTransaction()
            //transaction.replace(R.id.addDomainOverrideFragmentLayout, listFragment)
            //transaction.commitAllowingStateLoss()

        } else {
            Toast.makeText(requireContext(), "Please add proper domain", Toast.LENGTH_SHORT).show()
        }
    }

    private fun domainCheck(domain: String): Boolean {
        return !domain.isEmpty()
    }

}