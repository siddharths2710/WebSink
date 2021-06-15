package com.sid.websink.fragments.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sid.websink.R
import com.sid.websink.data.PinnerViewModel
import com.sid.websink.fragments.add.addPinnerOverrideFragment
import kotlinx.android.synthetic.main.fragment_list_pinner_override.view.*


class ListPinnerOverrideFragment : Fragment() {

    private lateinit var mPinnerViewModel: PinnerViewModel
    override fun onStart() {
        super.onStart()
        activity?.actionBar?.title = "Cert Pinning Configs"
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_pinner_override, container, false)
        val adapter = ListPinnerOverrideAdapter()
        val recyclerView = view.pinnerRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        mPinnerViewModel = ViewModelProvider(this).get(PinnerViewModel::class.java)
        mPinnerViewModel.getAll.observe(viewLifecycleOwner, Observer { pinnerMapping ->
            adapter.setData(pinnerMapping)
        })
        view.pinnerMenuFab.setOnClickListener {
            //findNavController().navigate(R.id.action_listPinnerOverrideFragment_to_addPinnerOverrideFragment)
            val addFragment = addPinnerOverrideFragment()
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.listPinnerOverrideFragmentLayout, addFragment)
            transaction.addToBackStack(addFragment.toString())
            transaction.commitAllowingStateLoss()
        }
        return view
    }
}