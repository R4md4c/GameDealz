package de.r4md4c.gamedealz.common.decorator

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R

/**
 * An [androidx.recyclerview.widget.RecyclerView.ItemDecoration] used to add margins of 8 dp between items.
 */
class StaggeredGridDecorator(private val context: Context) : RecyclerView.ItemDecoration() {

    private val spacing4 by lazy {
        context.resources.getDimensionPixelSize(R.dimen.baseline_1x) / 2
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.paddingLeft != spacing4) {
            parent.setPadding(spacing4, spacing4, spacing4, spacing4)
            parent.clipToPadding = false
        }

        outRect.top = spacing4
        outRect.bottom = spacing4
        outRect.left = spacing4
        outRect.right = spacing4
    }
}
