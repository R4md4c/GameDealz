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
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import de.r4md4c.commonproviders.extensions.resolveThemeColor
import de.r4md4c.gamedealz.R
import kotlinx.android.synthetic.main.layout_detail_header_filter_item.view.*

typealias OnFilterItemClick = (Int) -> Unit

class FilterHeaderItem(
    private val headerString: String,
    @MenuRes private val menuItem: Int,
    @IdRes private val defaultChosenItem: Int,
    private val onFilterItemClick: OnFilterItemClick
) : AbstractItem<FilterHeaderItem, FilterHeaderItem.ViewHolder>() {

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_detail_header_filter_item

    override fun getViewHolder(v: View): FilterHeaderItem.ViewHolder = FilterHeaderItem.ViewHolder(v)

    override fun getLayoutRes(): Int = R.layout.layout_detail_header_filter_item

    override fun bindView(holder: FilterHeaderItem.ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        with(holder.itemView) {
            header.text = headerString
            val icon = AppCompatResources.getDrawable(context, R.drawable.ic_sort_list)?.apply {
                DrawableCompat.setTint(this, context.resolveThemeColor(R.attr.colorOnSurface))
            }
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                filterView, null,
                null, icon, null
            )

            filterView.setOnClickListener {
                PopupMenu(context, filterView).also { popupMenu ->
                    popupMenu.inflate(menuItem)
                    popupMenu.menu.findItem(defaultChosenItem)?.isChecked = true
                    popupMenu.show()
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        onFilterItemClick(menuItem.itemId)
                        menuItem.isChecked = true
                        true
                    }
                }
            }
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
