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

package de.r4md4c.gamedealz.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R
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
