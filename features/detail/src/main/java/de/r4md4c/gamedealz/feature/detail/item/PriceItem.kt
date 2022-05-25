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

package de.r4md4c.gamedealz.feature.detail.item

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.annotation.WorkerThread
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.newAndOldPriceSpan
import de.r4md4c.gamedealz.domain.model.formatCurrency
import de.r4md4c.gamedealz.feature.detail.R
import de.r4md4c.gamedealz.feature.detail.SortOrder
import de.r4md4c.gamedealz.feature.detail.databinding.LayoutDetailPricesItemBinding
import de.r4md4c.gamedealz.feature.detail.model.PriceDetails
import java.util.concurrent.TimeUnit

class PriceItem(
    private val historicalLowText: CharSequence?,
    private val currentBestText: CharSequence,
    private val buyUrl: String,
    private val shopName: String,
    private val sortOrder: SortOrder,
    private val onBuyClick: (String) -> Unit
) : AbstractItem<PriceItem, PriceItem.ViewHolder>() {

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_detail_prices_item

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v).apply {
        with(v) {
            binding.constraintLayout.loadLayoutDescription(R.xml.layout_detail_prices_item_state)
        }
    }

    override fun getLayoutRes(): Int = R.layout.layout_detail_prices_item

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        with(holder.binding) {
            val layoutState = when (sortOrder) {
                SortOrder.ByHistoricalLow -> R.id.state_historical_low
                SortOrder.ByCurrentPrice -> R.id.state_current_best
            }
            constraintLayout.setState(layoutState, 0, 0)

            val (currentBestAppearance, historicalLowAppearance) = when (layoutState) {
                R.id.state_historical_low -> {
                    R.style.TextAppearance_MaterialComponents_Body2 to R.style.TextAppearance_MaterialComponents_Body1
                }
                R.id.state_current_best -> {
                    R.style.TextAppearance_MaterialComponents_Body1 to R.style.TextAppearance_MaterialComponents_Body2
                }
                else -> throw IllegalStateException("Unsupported states provided.")
            }

            TextViewCompat.setTextAppearance(historicalLow, historicalLowAppearance)
            TextViewCompat.setTextAppearance(currentBest, currentBestAppearance)

            currentBest.text = currentBestText
            historicalLow.text = historicalLowText
            shop.text = shopName
            root.setOnClickListener { onBuyClick(buyUrl) }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = LayoutDetailPricesItemBinding.bind(itemView)
    }
}

@WorkerThread
fun PriceDetails.toPriceItem(
    resourcesProvider: ResourcesProvider,
    dateFormatter: DateFormatter,
    sortOrder: SortOrder,
    onBuyClick: (String) -> Unit
): PriceItem {
    val currentBestText = currentBestText(resourcesProvider)
    val historicalLowText = historicalLowText(resourcesProvider, dateFormatter)
    return PriceItem(
        historicalLowText,
        currentBestText,
        priceModel.url,
        shopModel.name,
        sortOrder,
        onBuyClick
    )
}

private fun PriceDetails.currentBestText(resourcesProvider: ResourcesProvider): CharSequence {
    val currentBestPrice =
        priceModel.newAndOldPriceSpan(
            currencyModel,
            resourcesProvider.getColor(R.color.newPriceColor),
            resourcesProvider.getColor(R.color.oldPriceColor)
        )
    return TextUtils.concat(
        resourcesProvider.getString(R.string.current_best),
        " ",
        currentBestPrice
    )
}

private fun PriceDetails.historicalLowText(
    resourcesProvider: ResourcesProvider,
    dateFormatter: DateFormatter
): CharSequence? {
    historicalLowModel ?: return null
    return TextUtils.concat(
        resourcesProvider.getString(R.string.historical_low),
        " ",
        SpannableString(historicalLowModel.price.formatCurrency(currencyModel))
            .apply {
                setSpan(
                    ForegroundColorSpan(resourcesProvider.getColor(R.color.newPriceColor)),
                    0,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(StyleSpan(Typeface.BOLD), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            },
        " ",
        resourcesProvider.getString(R.string.on),
        " ",
        SpannableString(
            dateFormatter.formatDateTime(
                TimeUnit.SECONDS.toMillis(historicalLowModel.added),
                DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_YEAR
            )
        )
            .apply {
                setSpan(
                    StyleSpan(Typeface.BOLD_ITALIC),
                    0,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            })
}
