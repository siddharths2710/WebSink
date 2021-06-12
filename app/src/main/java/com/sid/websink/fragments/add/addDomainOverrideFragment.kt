package com.sid.websink.fragments.add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sid.websink.R
import com.sid.websink.data.DomainOverrideMapping
import kotlinx.android.synthetic.main.fragment_add_domain_override.*
import kotlinx.android.synthetic.main.fragment_add_domain_override.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [addDomainOverrideFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class addDomainOverrideFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
      val view = inflater.inflate(R.layout.fragment_add_domain_override, container, false)
        view.domain_mapping_btn.setOnClickListener {
            insertContentstoDB()
        }
    }
    private fun insertContentstoDB() {
        val old_domain = old_domain_text.text.toString()
        val new_domain = new_domain_text.text.toString()
        if(domainCheck(old_domain) && domainCheck(new_domain)) {
            val domainMapping = DomainOverrideMapping(old_domain, new_domain)
        }
    }

    private fun domainCheck(domain: String): Boolean {
        return !domain.isEmpty()
    }

}