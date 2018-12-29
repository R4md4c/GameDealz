package de.r4md4c.gamedealz.detail.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R

class ScreenshotsItemDecorator(private val resourcesProvider: ResourcesProvider) : RecyclerView.ItemDecoration() {

    private val spacing4 by lazy { resourcesProvider.getDimenPixelSize(R.dimen.baseline_0_5x) }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        val gridLayoutManager = parent.layoutManager as? GridLayoutManager
        val spanCount = gridLayoutManager?.spanCount ?: -1
        val spanIndex = gridLayoutManager?.spanSizeLookup?.getSpanIndex(position, gridLayoutManager.spanCount) ?: -1

        with(outRect) {
            // If last span then don't add a right offset.
            right = if (spanIndex == spanCount - 1) 0 else spacing4
            // Ignore the top 3
            top = when (position) {
                in 0 until spanCount -> 0
                else -> spacing4
            }
        }

    }
}