package com.kotlin.userlocationpathdrawinggooglemaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.userlocationpathdrawinggooglemaps.databinding.FragmentListBinding

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
        val dataList = listOf("Item 1", "Item 2", "Item 3", "Item 4","item 5", "item 6")

        adapter = ListAdapter(dataList) // Set up your adapter
        recyclerView.adapter = adapter

        // Optionally, set your data here
        // adapter.submitList(yourDataList)

        return binding.root
    }
}
