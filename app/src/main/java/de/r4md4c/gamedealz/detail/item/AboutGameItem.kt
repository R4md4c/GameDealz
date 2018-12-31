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
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mikepenz.fastadapter.items.AbstractItem
import de.r4md4c.gamedealz.R
import kotlinx.android.synthetic.main.layout_about_game_item.view.*

class AboutGameItem(
    private val headerImage: String?,
    private val shortDescription: String
) : AbstractItem<AboutGameItem, AboutGameItem.ViewHolder>() {

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_about_game_item

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    override fun getLayoutRes(): Int = R.layout.layout_about_game_item

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        with(holder.itemView) {
            if (headerImage == null) {
                GlideApp.with(this).clear(image)
            } else {
                GlideApp.with(this)
                    .load(headerImage)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progress.isVisible = false
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progress.isVisible = false
                            return false
                        }
                    })
                    .into(image)
            }

            description.text = HtmlCompat.fromHtml(shortDescription, HtmlCompat.FROM_HTML_MODE_COMPACT)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}