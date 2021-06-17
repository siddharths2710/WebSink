package com.sid.websink.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sid.websink.R
import com.sid.websink.data.PinnerMapping
import kotlinx.android.synthetic.main.pin_override_row.view.*

class ListPinnerOverrideAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1
    private var pinnerOverrideList = emptyList<PinnerMapping>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == TYPE_HEADER) {
            return HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.pin_override_header, parent, false))
        }
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.pin_override_row, parent, false))
    }

    override fun getItemCount(): Int {
        return pinnerOverrideList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0)
            return TYPE_HEADER
        return TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is ItemViewHolder) {
            val currentItem = pinnerOverrideList[position - 1]
            holder.itemView.pinnerRowIdView.text = position.toString()
            holder.itemView.pinnerDomainRowView.text = currentItem.domain.toString()
            holder.itemView.pinnerHashRowView.text = currentItem.hashType.toString()
        }

    }

    fun setData(pinnerMapping: List<PinnerMapping>) {
        this.pinnerOverrideList = pinnerMapping
        notifyDataSetChanged()
    }

    private class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}
    private class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}
}