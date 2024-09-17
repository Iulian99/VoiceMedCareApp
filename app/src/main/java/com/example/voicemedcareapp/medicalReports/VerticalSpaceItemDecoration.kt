package com.example.voicemedcareapp.medicalReports

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// Adaugă spațiu doar sub fiecare item, cu excepția ultimului item
class VerticalSpaceItemDecoration (private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
            outRect.bottom = verticalSpaceHeight
        }
    }
}