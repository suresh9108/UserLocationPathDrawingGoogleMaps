package com.kotlin.userlocationpathdrawinggooglemaps.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

class SnapOnScrollListener(
    private val snapHelper: LinearSnapHelper,
    private val listener: OnSnapPositionChangeListener
) : RecyclerView.OnScrollListener() {

    private var previousPosition: Int = RecyclerView.NO_POSITION

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val snappedView = snapHelper.findSnapView(layoutManager) ?: return
        val position = layoutManager.getPosition(snappedView)

        if (position != previousPosition) {
            previousPosition = position
            listener.onSnapPositionChange(position)
        }
    }
}
