package com.kotlin.userlocationpathdrawinggooglemaps.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.userlocationpathdrawinggooglemaps.R
import com.kotlin.userlocationpathdrawinggooglemaps.model.DataSet

class ListAdapter(private val dataList: List<DataSet>) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    // ViewHolder class to hold the views for each item in the list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.plugName)
        val locationText: TextView = itemView.findViewById(R.id.locationText)
        val logoImage: ImageView = itemView.findViewById(R.id.logoImage)
    }

    // Create the ViewHolder for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    // Bind the data to the views in the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = dataList[position].title
        holder.locationText.text = dataList[position].location
        holder.logoImage.setImageResource(dataList[position].img)
    }

    // Return the number of items in the list
    override fun getItemCount(): Int {
        return dataList.size
    }
}
