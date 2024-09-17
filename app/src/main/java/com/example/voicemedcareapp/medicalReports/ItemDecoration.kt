package com.example.voicemedcareapp.medicalReports

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BottomSpaceItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == parent.adapter?.itemCount?.minus(1)) {
            outRect.bottom = spaceHeight // Adaugă spațiul doar la ultimul element
        } else {
            outRect.bottom = 0 // Fără spațiu suplimentar pentru celelalte elemente
        }
    }
}
