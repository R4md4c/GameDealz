package de.r4md4c.gamedealz.deals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.DealModel
import kotlinx.android.synthetic.main.layout_deal_item.view.*

class DealsAdapter : PagedListAdapter<DealModel, DealsAdapter.DealViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DealViewHolder(layoutInflater.inflate(R.layout.layout_deal_item, parent, false))
    }

    override fun onBindViewHolder(holder: DealViewHolder, position: Int) {
        val item = getItem(position)

        with(holder.itemView) {
            title.text = item?.title
        }
    }

    class DealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

private val COMPARATOR = object : DiffUtil.ItemCallback<DealModel>() {

    override fun areItemsTheSame(oldItem: DealModel, newItem: DealModel): Boolean =
        oldItem.gameId == newItem.gameId

    override fun areContentsTheSame(oldItem: DealModel, newItem: DealModel): Boolean = oldItem == newItem
}