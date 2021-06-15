package com.sid.websink.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sid.websink.R
import com.sid.websink.data.DomainOverrideMapping
import kotlinx.android.synthetic.main.domain_override_header.view.*
import kotlinx.android.synthetic.main.domain_override_row.view.*

class ListDomainOverrideAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1
    private var domainOverrideList = emptyList<DomainOverrideMapping>()

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == TYPE_HEADER) {
            return HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.domain_override_header, parent, false))
        }
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.domain_override_row, parent, false))
    }

    override fun getItemCount(): Int {
        return domainOverrideList.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        /*val currentItem = domainOverrideList[position]
        holder.itemView.oldDomainRowView.text = currentItem.oldDomain.toString()
        holder.itemView.newDomainRowView.text = currentItem.newDomain.toString()*/
        if(holder is ItemViewHolder) {
            val currentItem = domainOverrideList[position - 1]
            holder.itemView.oldDomainRowView.text = currentItem.oldDomain
            holder.itemView.newDomainRowView.text = currentItem.newDomain.toString()
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0)
            return TYPE_HEADER
        return TYPE_ITEM
    }

    fun setData(domainOverrideMapping: List<DomainOverrideMapping>) {
        this.domainOverrideList = domainOverrideMapping
        notifyDataSetChanged()
    }

    private class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}
    private class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}
}