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

package de.r4md4c.gamedealz.feature.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import de.r4md4c.gamedealz.feature.search.model.SearchItemRenderModel

class SearchAdapter(
    private val layoutInflater: LayoutInflater,
    private val onClickListener: (SearchItemRenderModel) -> Unit
) : ListAdapter<SearchItemRenderModel, SearchItemViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
        return layoutInflater.inflate(R.layout.layout_search_result_item, parent, false).run {
            SearchItemViewHolder(this)
        }
    }

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {
        val searchItem = getItem(position)
        holder.onBind(searchItem)
        holder.itemView.setOnClickListener { onClickListener(searchItem) }
    }
}

private val COMPARATOR = object : DiffUtil.ItemCallback<SearchItemRenderModel>() {

    override fun areItemsTheSame(oldItem: SearchItemRenderModel, newItem: SearchItemRenderModel): Boolean =
        oldItem.gameId == newItem.gameId

    override fun areContentsTheSame(oldItem: SearchItemRenderModel, newItem: SearchItemRenderModel): Boolean =
        oldItem == newItem
}
