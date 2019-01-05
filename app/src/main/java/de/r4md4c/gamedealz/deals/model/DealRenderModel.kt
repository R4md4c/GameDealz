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

package de.r4md4c.gamedealz.deals.model

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.style.StyleSpan
import androidx.annotation.ColorRes
import androidx.annotation.WorkerThread
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.newAndOldPriceSpan
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.toPriceModel

data class DealRenderModel(
    val gameId: String,
    val title: CharSequence,
    val newPrice: CharSequence?,
    val storesAndTime: CharSequence?,
    val imageUrl: String?,
    val buyUrl: String,
    val priceModel: PriceModel
)

/**
 * This method should be called on a worker thread, to not create the span on the Main thread.
 */
@WorkerThread
fun DealModel.toRenderModel(
    resourcesProvider: ResourcesProvider,
    @ColorRes newPriceColorRes: Int,
    @ColorRes oldPriceColorRest: Int
) =
    DealRenderModel(
        gameId,
        title,
        newAndOldPriceSpan(resourcesProvider.getColor(newPriceColorRes), resourcesProvider.getColor(oldPriceColorRest)),
        storeAndTimeSpan(resourcesProvider),
        urls.imageUrl,
        urls.buyUrl,
        toPriceModel()
    )

private fun DealModel.storeAndTimeSpan(resourcesProvider: ResourcesProvider): Spannable {
    val timestampString = DateUtils.getRelativeTimeSpanString(added * 1000)

    return SpannableStringBuilder()
        .append(resourcesProvider.getString(R.string.on))
        .append(' ')
        .append(shop.name)
        .apply {
            setSpan(
                StyleSpan(Typeface.BOLD),
                length - shop.name.length,
                length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        .append(' ')
        .append(resourcesProvider.getString(R.string.since))
        .append(' ')
        .append(timestampString)
        .apply {
            setSpan(
                StyleSpan(Typeface.BOLD),
                length - timestampString.length,
                length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

}