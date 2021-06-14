package com.sid.websink.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sid.websink.R
import com.sid.websink.data.PinnerMapping
import kotlinx.android.synthetic.main.pin_override_row.view.*

class ListPinnerOverrideAdapter: RecyclerView.Adapter<ListPinnerOverrideAdapter.MyViewHolder>() {

    private var pinnerOverrideList = emptyList<PinnerMapping>()

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.pin_override_row, parent, false))
    }

    override fun getItemCount(): Int {
        return pinnerOverrideList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = pinnerOverrideList[position]
        holder.itemView.pinnerDomainRowView.text = currentItem.domain.toString()
        holder.itemView.pinnerFingerprintRowView.text = "${currentItem.hashType.toString()}/${currentItem.hashVal.toString()}"
    }

    fun setData(pinnerMapping: List<PinnerMapping>) {
        this.pinnerOverrideList = pinnerMapping
        notifyDataSetChanged()
    }
}