package com.kotlin.userlocationpathdrawinggooglemaps.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.kotlin.userlocationpathdrawinggooglemaps.R

class HorizantalListAdapter(private val dataList: List<LatLng>) : RecyclerView.Adapter<HorizantalListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.locationText) // Replace with your actual view id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.horizantal_item_layout, parent, false)

        val displayMetrics = parent.context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val itemWidth = (screenWidth * 0.85).toInt()

        view.layoutParams.width = itemWidth

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = dataList[position].latitude.toString()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
