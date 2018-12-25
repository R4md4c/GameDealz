package de.r4md4c.gamedealz.common.decorator

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import de.r4md4c.gamedealz.R

/**
 * An [androidx.recyclerview.widget.RecyclerView.ItemDecoration] used to add margins of 8 dp between items.
 */
class GridDecorator(private val context: Context) : RecyclerView.ItemDecoration() {

    private val spacing8 by lazy {
        context.resources.getDimensionPixelSize(R.dimen.baseline_1x)
    }

    private val spanCount by lazy {
        context.resources.getInteger(R.integer.span_count)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        (view.layoutParams as? StaggeredGridLayoutManager.LayoutParams)?.apply {
            outRect.left = spacing8
            outRect.top = spacing8
            outRect.right = if (spanIndex % spanCount == 0) 0 else spacing8
        }
    }
}
