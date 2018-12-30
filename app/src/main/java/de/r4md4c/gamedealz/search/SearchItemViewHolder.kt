package de.r4md4c.gamedealz.search

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.inSpans
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.image.GlideApp
import de.r4md4c.gamedealz.common.newAndOldPriceSpan
import de.r4md4c.gamedealz.domain.model.SearchResultModel
import de.r4md4c.gamedealz.domain.model.formatCurrency
import kotlinx.android.synthetic.main.layout_search_result_item.view.*
import java.util.concurrent.TimeUnit

class SearchItemViewHolder(
    itemView: View,
    private val resourcesProvider: ResourcesProvider,
    private val dateFormatter: DateFormatter
) : RecyclerView.ViewHolder(itemView) {

    private val newPriceColor by lazy { ContextCompat.getColor(itemView.context, R.color.newPriceColor) }
    private val oldPriceColor by lazy { ContextCompat.getColor(itemView.context, R.color.oldPriceColor) }

    fun onBind(searchResultModel: SearchResultModel) {
        with(itemView) {
            name.text = searchResultModel.title
            currentBest.text = searchResultModel.currentBest()
            historicalLow.text = searchResultModel.historicalLow()

            GlideApp.with(image)
                .load(searchResultModel.imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(image)
        }
    }

    private fun SearchResultModel.currentBest(): Spannable? {
        val currentBest = prices.firstOrNull() ?: return null
        val shop = currentBest.shop.name

        return SpannableStringBuilder()
            .append(resourcesProvider.getString(R.string.current_best))
            .append(' ')
            .append(currentBest.newAndOldPriceSpan(currencyModel, newPriceColor, oldPriceColor))
            .append(' ')
            .append(resourcesProvider.getString(R.string.on).toLowerCase())
            .append(' ')
            .append(shop)
            .apply {
                val (start, end) = length - shop.length to length
                setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
    }

    private fun SearchResultModel.historicalLow(): Spannable? {
        val historicalLow = historicalLow ?: return null
        val shop = historicalLow.shop.name
        val price = historicalLow.price.formatCurrency(currencyModel) ?: return null
        val addedDate = dateFormatter.formatDateTime(
            TimeUnit.SECONDS.toMillis(historicalLow.added),
            DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_YEAR
        )

        return SpannableStringBuilder()
            .append(resourcesProvider.getString(R.string.historical_low))
            .append(' ')
            .inSpans(StyleSpan(Typeface.BOLD), ForegroundColorSpan(newPriceColor)) {
                append(price)
            }
            .append(' ')
            .append(resourcesProvider.getString(R.string.on).toLowerCase())
            .append(' ')
            .inSpans(StyleSpan(Typeface.BOLD)) {
                append(shop)
            }
            .append(' ')
            .append(resourcesProvider.getString(R.string.on).toLowerCase())
            .append(' ')
            .inSpans(StyleSpan(Typeface.BOLD_ITALIC)) {
                append(addedDate)
            }
    }
}
