package com.sid.websink.fragments.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sid.websink.R
import com.sid.websink.data.DomainOverrideViewModel
import kotlinx.android.synthetic.main.fragment_list_domain_override.view.*
import kotlinx.android.synthetic.main.fragment_list_pinner_override.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.sid.websink.fragments.add.addDomainOverrideFragment


class ListDomainOverrideFragment : Fragment() {

    private lateinit var mDomainOverrideViewModel: DomainOverrideViewModel


    override fun onStart() {
        super.onStart()
        activity?.actionBar?.title = "Domain Overrides"
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_domain_override, container, false)
        val adapter = ListDomainOverrideAdapter()
        val recyclerView = view.domainOverrideRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        mDomainOverrideViewModel =  ViewModelProvider(this).get(DomainOverrideViewModel::class.java)
        mDomainOverrideViewModel.getAll.observe(viewLifecycleOwner, Observer { domainOverrideMapping ->
            adapter.setData(domainOverrideMapping)
        })

        view.domainOverrideMenuFab.show()
        view.domainOverrideMenuFab.setOnClickListener {
            val addFragment = addDomainOverrideFragment()
            val transaction = childFragmentManager.beginTransaction()
            view.domainOverrideMenuFab.hide()
            transaction.replace(R.id.listDomainOverrideFragmentLayout, addFragment)
            transaction.addToBackStack(addFragment.toString())
            transaction.commitAllowingStateLoss()
        }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        childFragmentManager.setFragmentResult(
            "trusted_fragment",
            bundleOf("REFRESH_MAIN" to true)
        )
    }

}