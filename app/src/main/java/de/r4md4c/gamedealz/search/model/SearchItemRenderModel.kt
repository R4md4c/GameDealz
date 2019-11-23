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

package de.r4md4c.gamedealz.search.model

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorRes
import androidx.annotation.WorkerThread
import androidx.core.text.inSpans
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.newAndOldPriceSpan
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.SearchResultModel
import de.r4md4c.gamedealz.domain.model.formatCurrency
import java.util.concurrent.TimeUnit

data class SearchItemRenderModel(
    val gameId: String,
    val title: CharSequence,
    val currentBest: CharSequence?,
    val historicalLow: CharSequence?,
    val imageUrl: String?,
    val currentBestPriceModel: PriceModel?
)

@WorkerThread
fun SearchResultModel.toRenderModel(
    resourcesProvider: ResourcesProvider,
    dateFormatter: DateFormatter
): SearchItemRenderModel =
    SearchItemRenderModel(
        gameId, title,
        currentBest(resourcesProvider, R.color.newPriceColor, R.color.oldPriceColor),
        historicalLow(resourcesProvider, dateFormatter, R.color.newPriceColor),
        imageUrl,
        prices.firstOrNull()
    )

private fun SearchResultModel.currentBest(
    resourcesProvider: ResourcesProvider,
    @ColorRes newPriceColor: Int,
    @ColorRes oldPriceColor: Int
): Spannable? {
    val currentBest = prices.firstOrNull() ?: return null
    val shop = currentBest.shop.name

    return SpannableStringBuilder()
        .append(resourcesProvider.getString(R.string.current_best))
        .append(' ')
        .append(
            currentBest.newAndOldPriceSpan(
                currencyModel,
                resourcesProvider.getColor(newPriceColor),
                resourcesProvider.getColor(oldPriceColor)
            )
        )
        .append(' ')
        .append(resourcesProvider.getString(R.string.on).toLowerCase())
        .append(' ')
        .append(shop)
        .apply {
            val (start, end) = length - shop.length to length
            setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
}

private fun SearchResultModel.historicalLow(
    resourcesProvider: ResourcesProvider,
    dateFormatter: DateFormatter,
    @ColorRes newPriceColor: Int
): Spannable? {
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
        .inSpans(StyleSpan(Typeface.BOLD), ForegroundColorSpan(resourcesProvider.getColor(newPriceColor))) {
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
