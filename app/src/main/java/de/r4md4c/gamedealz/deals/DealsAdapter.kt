package de.r4md4c.gamedealz.deals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.utils.state.StateMachineDelegate
import de.r4md4c.gamedealz.utils.viewholder.ProgressViewHolder

class DealsAdapter(private val stateMachine: StateMachineDelegate) :
    PagedListAdapter<DealModel, RecyclerView.ViewHolder>(COMPARATOR) {
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
            R.layout.layout_deal_item -> (holder as DealItemViewHolder).onBind(getItem(position))
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

private val COMPARATOR = object : DiffUtil.ItemCallback<DealModel>() {

    override fun areItemsTheSame(oldItem: DealModel, newItem: DealModel): Boolean =
        oldItem.gameId == newItem.gameId

    override fun areContentsTheSame(oldItem: DealModel, newItem: DealModel): Boolean = oldItem == newItem
}