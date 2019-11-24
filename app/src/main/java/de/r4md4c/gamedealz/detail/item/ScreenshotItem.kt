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
import de.r4md4c.gamedealz.common.image.GlideApp
import de.r4md4c.gamedealz.domain.model.ScreenshotModel
import kotlinx.android.synthetic.main.layout_screenshot_item.view.*

typealias OnScreenShotClick = (Int) -> Unit

class ScreenshotItem(
    private val screenshotModel: ScreenshotModel,
    private val screenshotPosition: Int,
    private val onScreenShotClick: OnScreenShotClick
) : AbstractItem<ScreenshotItem, ScreenshotItem.ViewHolder>() {

    init {
        withIdentifier(screenshotModel.hashCode().toLong())
    }

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        with(holder.itemView) {
            image.setOnClickListener { onScreenShotClick(screenshotPosition) }
            GlideApp.with(image)
                .load(screenshotModel.thumbnail)
                .placeholder(R.drawable.ic_placeholder)
                .centerCrop()
                .into(image)
        }
    }

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_screenshot_item

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    override fun getLayoutRes(): Int = R.layout.layout_screenshot_item

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
