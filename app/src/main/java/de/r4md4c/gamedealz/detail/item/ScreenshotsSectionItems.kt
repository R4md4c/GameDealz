package de.r4md4c.gamedealz.detail.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.detail.adapter.ScreenshotsAdapter
import de.r4md4c.gamedealz.detail.decorator.ScreenshotsItemDecorator
import de.r4md4c.gamedealz.domain.model.ScreenshotModel
import kotlinx.android.synthetic.main.layout_screenshots_section.view.*

class ScreenshotsSectionItems(
    private val screenshots: List<ScreenshotModel>,
    private val resourcesProvider: ResourcesProvider,
    private val parentPool: RecyclerView.RecycledViewPool
) : AbstractItem<ScreenshotsSectionItems, ScreenshotsSectionItems.ViewHolder>() {

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_screenshots_section

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v).apply {
        with(itemView) {
            with(recyclerView) {
                adapter = ScreenshotsAdapter(LayoutInflater.from(context)).apply { submitList(screenshots) }
                setRecycledViewPool(parentPool)
                addItemDecoration(ScreenshotsItemDecorator(resourcesProvider))
                layoutManager = GridLayoutManager(context, 3).apply { spanSizeLookup.isSpanIndexCacheEnabled = true }
            }
        }
    }

    override fun getLayoutRes(): Int = R.layout.layout_screenshots_section

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}