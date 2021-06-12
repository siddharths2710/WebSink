package com.sid.websink.fragments.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.sid.websink.R
import kotlinx.android.synthetic.main.fragment_list_domain_override.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [listPinnerOverrideFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class listPinnerOverrideFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_pinner_override, container, false)
        view.domainOverrideMenuFab.setOnClickListener {
            findNavController().navigate(R.id.action_listPinnerOverrideFragment_to_addPinnerOverrideFragment)
        }
    }
}