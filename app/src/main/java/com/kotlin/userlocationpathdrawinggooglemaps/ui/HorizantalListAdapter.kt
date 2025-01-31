package com.kotlin.userlocationpathdrawinggooglemaps.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.kotlin.userlocationpathdrawinggooglemaps.R

class HorizantalListAdapter(private val dataList: List<LatLng>) : RecyclerView.Adapter<HorizantalListAdapter.ViewHolder>() {

    // ViewHolder class to hold the views for each item in the list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.locationText) // Replace with your actual view id
    }

    // Create the ViewHolder for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.horizantal_item_layout, parent, false)
        //view.layoutParams = ViewGroup.LayoutParams((parent.width * 0.7).toInt(),ViewGroup.LayoutParams.MATCH_PARENT)

        val displayMetrics = parent.context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val itemWidth = (screenWidth * 0.8).toInt()

        // Set the width of the item
        view.layoutParams.width = itemWidth

        return ViewHolder(view)
    }

    // Bind the data to the views in the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = dataList[position].latitude.toString()
    }

    // Return the number of items in the list
    override fun getItemCount(): Int {
        return dataList.size
    }
}
