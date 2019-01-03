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
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import de.r4md4c.gamedealz.R
import kotlinx.android.synthetic.main.layout_detail_header.view.*

class HeaderItem(private val headerString: String) : AbstractItem<HeaderItem, HeaderItem.ViewHolder>() {

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_detail_header

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    override fun getLayoutRes(): Int = R.layout.layout_detail_header

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        with(holder.itemView) {
            header.text = headerString
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}