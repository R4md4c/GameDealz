package de.r4md4c.gamedealz.deals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.DealModel

class DealsAdapter : PagedListAdapter<DealModel, DealItemViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DealItemViewHolder(layoutInflater.inflate(R.layout.layout_deal_item, parent, false))
    }

    override fun onBindViewHolder(holder: DealItemViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}

private val COMPARATOR = object : DiffUtil.ItemCallback<DealModel>() {

    override fun areItemsTheSame(oldItem: DealModel, newItem: DealModel): Boolean =
        oldItem.gameId == newItem.gameId

    override fun areContentsTheSame(oldItem: DealModel, newItem: DealModel): Boolean = oldItem == newItem
}