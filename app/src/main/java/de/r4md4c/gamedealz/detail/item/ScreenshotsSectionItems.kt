package de.r4md4c.gamedealz.detail.item

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.mikepenz.fastadapter.items.AbstractItem
import com.stfalcon.imageviewer.StfalconImageViewer
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.image.GlideApp
import de.r4md4c.gamedealz.detail.adapter.ScreenshotsAdapter
import de.r4md4c.gamedealz.detail.decorator.ScreenshotsItemDecorator
import de.r4md4c.gamedealz.domain.model.ScreenshotModel
import kotlinx.android.synthetic.main.layout_screenshots_section.view.*

typealias OnScreenShotClick = (Int) -> Unit

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
                adapter = ScreenshotsAdapter(LayoutInflater.from(context), onScreenShotClick(context))
                    .apply {
                        submitList(screenshots)
                    }
                setRecycledViewPool(parentPool)
                addItemDecoration(ScreenshotsItemDecorator(resourcesProvider))
                layoutManager =
                        GridLayoutManager(context, resourcesProvider.getInteger(R.integer.screenshots_span_count))
                    .apply { spanSizeLookup.isSpanIndexCacheEnabled = true }
            }
        }
    }

    override fun getLayoutRes(): Int = R.layout.layout_screenshots_section

    private fun onScreenShotClick(context: Context): OnScreenShotClick = { position ->
        StfalconImageViewer.Builder<ScreenshotModel>(context, screenshots) { view, image ->
            val circularProgressDrawable = CircularProgressDrawable(context).apply {
                strokeWidth = resourcesProvider.getDimension(R.dimen.progress_stroke_size)
                centerRadius = resourcesProvider.getDimension(R.dimen.progress_size)
                setColorSchemeColors(ContextCompat.getColor(context, R.color.colorAccent))
                start()
            }
            GlideApp.with(view)
                .load(image.full)
                .placeholder(circularProgressDrawable)
                .into(view)
        }.also {
            it.withStartPosition(position)
                .show()
        }
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}