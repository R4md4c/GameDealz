package de.r4md4c.gamedealz.detail.decorator

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R

class DetailsItemDecorator(private val context: Context) : RecyclerView.ItemDecoration() {

    private val spacing8 by lazy { context.resources.getDimensionPixelSize(R.dimen.baseline_1x) }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: -1

        outRect.left = spacing8
        outRect.right = spacing8
        outRect.top = spacing8
        outRect.bottom = if (position == itemCount - 1) spacing8 else 0
    }
}