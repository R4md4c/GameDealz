package de.r4md4c.gamedealz.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.image.GlideApp
import de.r4md4c.gamedealz.detail.item.OnScreenShotClick
import de.r4md4c.gamedealz.domain.model.ScreenshotModel
import kotlinx.android.synthetic.main.layout_screenshot_item.view.*

class ScreenshotsAdapter(
    private val layoutInflater: LayoutInflater,
    private val onClick: OnScreenShotClick
) :
    ListAdapter<ScreenshotModel, RecyclerView.ViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ScreenshotViewHolder(layoutInflater.inflate(R.layout.layout_screenshot_item, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(holder.itemView) {
            GlideApp.with(this)
                .load(getItem(position).thumbnail)
                .placeholder(R.drawable.ic_placeholder)
                .into(image)
            image.setOnClickListener { onClick(position) }
        }
    }

    private class ScreenshotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

private val COMPARATOR = object : DiffUtil.ItemCallback<ScreenshotModel>() {
    override fun areItemsTheSame(oldItem: ScreenshotModel, newItem: ScreenshotModel): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: ScreenshotModel, newItem: ScreenshotModel): Boolean = oldItem == newItem
}
