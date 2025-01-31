package com.kotlin.userlocationpathdrawinggooglemaps.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.userlocationpathdrawinggooglemaps.R
import com.kotlin.userlocationpathdrawinggooglemaps.databinding.FragmentListBinding
import com.kotlin.userlocationpathdrawinggooglemaps.model.DataSet

class ListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListAdapter // Replace with your actual adapter class

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentListBinding = FragmentListBinding.inflate(inflater, container, false)

        // Set up the RecyclerView
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        val data= mutableListOf<DataSet>()
        data.add(DataSet("MAHINDRA CHARGER","Jayanagar, bangaluru", R.drawable.charging1))
        data.add(DataSet("GBT EV Charging Plug","JP Nagar, Bengaluru", R.drawable.charging1))
        data.add(DataSet("Okaya Charging Station","C Block, Sector 2, Shivajinagar",
            R.drawable.charging1
        ))
        data.add(DataSet("Charzer Charging Station","Jayanagara 9th Block, Bengaluru",
            R.drawable.charging1
        ))
        data.add(DataSet("Okaya Charging Station","C Block, Sector 2, Shivajinagar",
            R.drawable.charging1
        ))
        data.add(DataSet("Charzer Charging Station","Jayanagara 9th Block, Bengaluru",
            R.drawable.charging1
        ))


        adapter = ListAdapter(data) // Set up your adapter
        recyclerView.adapter = adapter



        return binding.root
    }
}
