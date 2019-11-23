/*
 * This file is part of GameDealz.
 *
 * GameDealz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * GameDealz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GameDealz.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.r4md4c.gamedealz.detail.decorator

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R

class DetailsFragmentItemDecorator(private val context: Context) : RecyclerView.ItemDecoration() {

    private val spacing8 by lazy { context.resources.getDimensionPixelSize(R.dimen.baseline_1x) }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        val viewType = parent.getChildViewHolder(view).itemViewType
        val itemCount = parent.adapter?.itemCount ?: return
        val gridLayoutManager = parent.layoutManager as? GridLayoutManager ?: return
        val spanIndex = gridLayoutManager.spanSizeLookup.getSpanIndex(position, gridLayoutManager.spanCount)

        outRect.left = spacing8
        outRect.right = getRight(spanIndex, viewType)
        outRect.top = getTop(position, viewType)
        outRect.bottom = getBottom(position, viewType, itemCount)
    }

    private fun getTop(position: Int, viewType: Int) =
        if (position == 0 && viewType == R.layout.layout_detail_header_filter_item) {
            spacing8 * 4
        } else spacing8

    private fun getBottom(position: Int, viewType: Int, itemCount: Int) =
        when {
            viewType == R.layout.layout_screenshot_item -> 0
            position == itemCount - 1 -> spacing8
            else -> 0
        }

    private fun getRight(spanIndex: Int, viewType: Int) =
        if (viewType == R.layout.layout_screenshot_item) {
            when (spanIndex) {
                2 -> spacing8
                else -> 0
            }
        } else {
            spacing8
        }
}
