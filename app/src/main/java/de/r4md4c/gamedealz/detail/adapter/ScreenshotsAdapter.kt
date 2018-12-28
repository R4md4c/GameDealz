package de.r4md4c.gamedealz.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.image.GlideApp
import kotlinx.android.synthetic.main.layout_screenshot_item.view.*

class ScreenshotsAdapter(private val layoutInflater: LayoutInflater) :
    ListAdapter<String, RecyclerView.ViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ScreenshotViewHolder(layoutInflater.inflate(R.layout.layout_screenshot_item, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(holder.itemView) {
            GlideApp.with(this)
                .load(getItem(position))
                .placeholder(R.drawable.ic_placeholder)
                .into(image)
        }
    }

    private class ScreenshotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

private val COMPARATOR = object : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
}