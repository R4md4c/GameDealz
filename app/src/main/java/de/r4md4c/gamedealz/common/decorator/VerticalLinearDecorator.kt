package de.r4md4c.gamedealz.common.decorator

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R

class VerticalLinearDecorator(private val context: Context) : RecyclerView.ItemDecoration() {

    private val spacing8 by lazy { context.resources.getDimensionPixelSize(R.dimen.baseline_1x) }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)

        outRect.left = spacing8
        outRect.right = spacing8
        outRect.top = if (position == 0) spacing8 else 0
        outRect.bottom = spacing8
    }

}