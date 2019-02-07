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

package de.r4md4c.gamedealz.deals.item

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.mikepenz.fastadapter.items.AbstractItem
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.StoreModel

data class FilterItem(val storeModel: StoreModel) : AbstractItem<FilterItem, FilterItem.ViewHolder>() {

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        val isSelected = isSelected
        with(holder.itemView as Chip) {
            text = storeModel.name
            chipBackgroundColor =
                if (isSelected) ColorStateList.valueOf(storeModel.color) else ColorStateList.valueOf(Color.parseColor("#e5e5e5"))
        }
    }

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_deals_filter_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    override fun getLayoutRes(): Int = R.layout.layout_deals_filter_item

    override fun getIdentifier(): Long = storeModel.hashCode().toLong()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
