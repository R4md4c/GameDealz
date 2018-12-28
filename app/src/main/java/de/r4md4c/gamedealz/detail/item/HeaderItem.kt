package de.r4md4c.gamedealz.detail.item

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import de.r4md4c.gamedealz.R
import kotlinx.android.synthetic.main.layout_detail_header.view.*

class HeaderItem(@StringRes val headerResId: Int) : AbstractItem<HeaderItem, HeaderItem.ViewHolder>() {

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_detail_header

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    override fun getLayoutRes(): Int = R.layout.layout_detail_header

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        with(holder.itemView) {
            header.text = resources.getString(headerResId)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}