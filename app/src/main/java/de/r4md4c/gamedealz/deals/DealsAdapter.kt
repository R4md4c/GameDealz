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

package de.r4md4c.gamedealz.deals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.deals.item.ProgressViewHolder
import de.r4md4c.gamedealz.deals.model.DealRenderModel

class DealsAdapter(private val dealClick: (deal: DealRenderModel) -> Unit) :
    PagedListAdapter<DealRenderModel, RecyclerView.ViewHolder>(COMPARATOR) {
    private var progress = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.layout_deal_item -> DealItemViewHolder(
                layoutInflater.inflate(
                    R.layout.layout_deal_item,
                    parent,
                    false
                )
            )
            R.layout.item_load_more_progress -> ProgressViewHolder(
                layoutInflater.inflate(
                    R.layout.item_load_more_progress,
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Unknown view type found!")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.layout_deal_item -> (holder as DealItemViewHolder).onBind(getItem(position), dealClick)
            else -> Unit
        }
    }

    override fun getItemCount(): Int =
        super.getItemCount() + if (progress) 1 else 0

    override fun getItemViewType(position: Int): Int =
        if (position == itemCount - 1 && progress) {
            R.layout.item_load_more_progress
        } else {
            R.layout.layout_deal_item
        }

    fun showProgress(show: Boolean) {
        progress = show
        if (progress) {
            notifyItemInserted(itemCount)
        } else {
            notifyItemRemoved(itemCount)
        }
    }
}

private val COMPARATOR = object : DiffUtil.ItemCallback<DealRenderModel>() {

    override fun areItemsTheSame(oldItem: DealRenderModel, newItem: DealRenderModel): Boolean =
        oldItem.gameId == newItem.gameId

    override fun areContentsTheSame(oldItem: DealRenderModel, newItem: DealRenderModel): Boolean = oldItem == newItem
}
