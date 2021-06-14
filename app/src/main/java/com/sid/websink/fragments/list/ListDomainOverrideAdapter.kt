package com.sid.websink.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sid.websink.R
import com.sid.websink.data.DomainOverrideMapping
import kotlinx.android.synthetic.main.domain_override_row.view.*

class ListDomainOverrideAdapter: RecyclerView.Adapter<ListDomainOverrideAdapter.MyViewHolder>() {
    private var domainOverrideList = emptyList<DomainOverrideMapping>()

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.domain_override_row, parent, false))
    }

    override fun getItemCount(): Int {
        return domainOverrideList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = domainOverrideList[position]
        holder.itemView.oldDomainRowView.text = currentItem.oldDomain.toString()
        holder.itemView.newDomainRowView.text = currentItem.newDomain.toString()
    }

    fun setData(domainOverrideMapping: List<DomainOverrideMapping>) {
        this.domainOverrideList = domainOverrideMapping
        notifyDataSetChanged()
    }
}