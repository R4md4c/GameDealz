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

package de.r4md4c.gamedealz.detail.item

import android.annotation.SuppressLint
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import de.r4md4c.gamedealz.R
import kotlinx.android.synthetic.main.layout_expandable_screenshot_header_item.view.*

typealias OnExpandClick = ExpandableScreenshotsHeader.() -> Unit

class ExpandableScreenshotsHeader(
    private val showExpandIcon: Boolean,
    private val onExpandClick: OnExpandClick
) :
    AbstractItem<ExpandableScreenshotsHeader, ExpandableScreenshotsHeader.ViewHolder>() {

    var isExpanded = false

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        val item = this
        with(holder.itemView) {
            expand_icon.isVisible = showExpandIcon
            ViewCompat.animate(expand_icon).cancel()
            expand_icon.setOnClickListener {
                onExpandClick()
                ViewCompat.animate(expand_icon).rotation(item.rotation)
            }
            expand_icon.rotation = item.rotation
        }
    }

    private val rotation
        get() = if (isExpanded) 90f else 270f

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_expandable_screenshot_header_item

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    override fun getLayoutRes(): Int = R.layout.layout_expandable_screenshot_header_item

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
