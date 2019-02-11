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

import android.graphics.Color
import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import androidx.annotation.ColorInt
import androidx.annotation.WorkerThread
import de.r4md4c.gamedealz.common.newAndOldPriceSpan
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.toPriceModel

data class DealRenderModel(
    val gameId: String,
    val title: CharSequence,
    val newPrice: CharSequence?,
    val store: CharSequence,
    @ColorInt val storeColor: Int,
    val timestamp: CharSequence,
    val imageUrl: String?,
    val buyUrl: String,
    val percentageCut: CharSequence,
    val priceModel: PriceModel
)

/**
 * This method should be called on a worker thread, to not create the span on the Main thread.
 */
@WorkerThread
fun DealModel.toRenderModel(
    @ColorInt newPriceColor: Int,
    @ColorInt oldPriceColorRes: Int
) =
    DealRenderModel(
        gameId,
        title,
        newAndOldPriceSpan(newPriceColor, oldPriceColorRes),
        shop.name,
        Color.parseColor(shop.rgbColor),
        DateUtils.getRelativeTimeSpanString(
            added * 1000,
            System.currentTimeMillis(),
            MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ),
        urls.imageUrl,
        urls.buyUrl,
        "-$priceCutPercentage%",
        toPriceModel()
    )
