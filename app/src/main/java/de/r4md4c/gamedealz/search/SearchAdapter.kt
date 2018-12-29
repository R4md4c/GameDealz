package de.r4md4c.gamedealz.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.SearchResultModel

class SearchAdapter(
    private val layoutInflater: LayoutInflater,
    private val resourcesProvider: ResourcesProvider,
    private val dateFormatter: DateFormatter,
    private val onClickListener: (SearchResultModel) -> Unit
) :
    ListAdapter<SearchResultModel, SearchItemViewHolder>(COMPARATOR) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
        return layoutInflater.inflate(R.layout.layout_search_result_item, parent, false).run {
            SearchItemViewHolder(this, resourcesProvider, dateFormatter)
        }
    }

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {
        holder.onBind(getItem(position))
        holder.itemView.setOnClickListener { onClickListener(getItem(position)) }
    }

    override fun getItemId(position: Int): Long = getItem(position).gameId.hashCode().toLong()

}

private val COMPARATOR = object : DiffUtil.ItemCallback<SearchResultModel>() {

    override fun areItemsTheSame(oldItem: SearchResultModel, newItem: SearchResultModel): Boolean =
        oldItem.gameId == newItem.gameId

    override fun areContentsTheSame(oldItem: SearchResultModel, newItem: SearchResultModel): Boolean =
        oldItem == newItem
}