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

package de.r4md4c.gamedealz.common

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt
import androidx.core.text.inSpans
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.formatCurrency

fun PriceModel.newAndOldPriceSpan(currencyModel: CurrencyModel, @ColorInt newPriceColor: Int, @ColorInt oldPriceColor: Int): CharSequence? =
    newAndOldPriceSpannableString(newPrice, oldPrice, currencyModel, oldPriceColor, newPriceColor)

fun DealModel.newAndOldPriceSpan(@ColorInt newPriceColor: Int, @ColorInt oldPriceColor: Int): CharSequence? =
    newAndOldPriceSpannableString(newPrice, oldPrice, currencyModel, oldPriceColor, newPriceColor)

private fun newAndOldPriceSpannableString(
    newPrice: Float,
    oldPrice: Float,
    currencyModel: CurrencyModel,
    @ColorInt oldPriceColor: Int,
    @ColorInt newPriceColor: Int
): Spannable? {
    val formattedOldPrice = oldPrice.formatCurrency(currencyModel) ?: return null
    val formattedNewPrice = newPrice.formatCurrency(currencyModel) ?: return null

    return SpannableStringBuilder()
        .inSpans(ForegroundColorSpan(oldPriceColor), StrikethroughSpan()) {
            append(formattedOldPrice)
        }
        .append(' ')
        .inSpans(StyleSpan(Typeface.BOLD), ForegroundColorSpan(newPriceColor)) {
            append(formattedNewPrice)
        }
}