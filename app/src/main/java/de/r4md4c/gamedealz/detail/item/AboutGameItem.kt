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
import de.r4md4c.gamedealz.common.image.GlideApp
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