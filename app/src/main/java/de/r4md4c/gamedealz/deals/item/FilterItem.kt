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
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.StoreModel
import kotlinx.android.synthetic.main.layout_deals_filter_item.view.*

data class FilterItem(private val storeModel: StoreModel) : AbstractItem<FilterItem, FilterItem.ViewHolder>() {

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        val isSelected = isSelected
        with(holder.itemView) {
            text.text = storeModel.name
            text.isChecked = isSelected
        }
    }

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_deals_filter_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    override fun getLayoutRes(): Int = R.layout.layout_deals_filter_item

    override fun getIdentifier(): Long = storeModel.hashCode().toLong()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
