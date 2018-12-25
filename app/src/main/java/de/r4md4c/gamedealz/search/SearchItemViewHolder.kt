package de.r4md4c.gamedealz.search

import android.content.res.Resources
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.image.GlideApp
import de.r4md4c.gamedealz.common.newAndOldPriceSpan
import de.r4md4c.gamedealz.domain.model.SearchResultModel
import de.r4md4c.gamedealz.domain.model.formatCurrency
import kotlinx.android.synthetic.main.layout_search_result_item.view.*

class SearchItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val newPriceColor by lazy { ContextCompat.getColor(itemView.context, R.color.newPriceColor) }
    private val oldPriceColor by lazy { ContextCompat.getColor(itemView.context, R.color.oldPriceColor) }

    fun onBind(searchResultModel: SearchResultModel) {
        with(itemView) {
            name.text = searchResultModel.title
            currentBest.text = searchResultModel.currentBest(resources)
            historicalLow.text = searchResultModel.historicalLow(resources)

            GlideApp.with(image)
                .load(searchResultModel.imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(image)
        }
    }

    private fun SearchResultModel.currentBest(resources: Resources): Spannable? {
        val currentBest = prices.firstOrNull() ?: return null
        val shop = currentBest.shop.name

        return SpannableStringBuilder()
            .append(resources.getString(R.string.current_best))
            .append(' ')
            .append(currentBest.newAndOldPriceSpan(currencyModel, newPriceColor, oldPriceColor))
            .append(' ')
            .append(resources.getString(R.string.on).toLowerCase())
            .append(' ')
            .append(shop)
            .apply {
                val (start, end) = length - shop.length to length
                setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
    }

    private fun SearchResultModel.historicalLow(resources: Resources): Spannable? {
        val historicalLow = historicalLow ?: return null
        val shop = historicalLow.shop.name
        val price = historicalLow.price.formatCurrency(currencyModel) ?: return null

        return SpannableStringBuilder()
            .append(resources.getString(R.string.historical_low))
            .append(' ')
            .append(price, StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            .apply {
                setSpan(
                    ForegroundColorSpan(newPriceColor),
                    length - price.length,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            .append(' ')
            .append(resources.getString(R.string.on).toLowerCase())
            .append(' ')
            .append(shop)
            .apply {
                val (start, end) = length - shop.length to length
                setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
    }
}
