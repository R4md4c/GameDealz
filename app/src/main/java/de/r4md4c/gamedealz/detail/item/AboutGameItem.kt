package de.r4md4c.gamedealz.detail.item

import android.annotation.SuppressLint
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
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
                    .into(image)
            }

            description.text = HtmlCompat.fromHtml(shortDescription, HtmlCompat.FROM_HTML_MODE_COMPACT)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}