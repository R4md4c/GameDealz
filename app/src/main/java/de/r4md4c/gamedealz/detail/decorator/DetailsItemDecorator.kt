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
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R

class DetailsItemDecorator(private val context: Context) : RecyclerView.ItemDecoration() {

    private val spacing8 by lazy { context.resources.getDimensionPixelSize(R.dimen.baseline_1x) }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val viewType = parent.getChildViewHolder(view).itemViewType
        val itemCount = parent.adapter?.itemCount ?: -1

        outRect.left = spacing8
        outRect.right = spacing8
        outRect.top =
                if (position == 0 && viewType == R.layout.layout_detail_header_filter_item) spacing8 * 4 else spacing8
        outRect.bottom = if (position == itemCount - 1) spacing8 else 0
    }
}