package de.r4md4c.gamedealz.common

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.formatCurrency

fun PriceModel.newAndOldPriceSpan(currencyModel: CurrencyModel, @ColorInt newPriceColor: Int, @ColorInt oldPriceColor: Int): Spannable? =
    newAndOldPriceSpannableString(newPrice, oldPrice, currencyModel, oldPriceColor, newPriceColor)

fun DealModel.newAndOldPriceSpan(@ColorInt newPriceColor: Int, @ColorInt oldPriceColor: Int): Spannable? =
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
        .append(formattedOldPrice, StrikethroughSpan(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        .apply {
            setSpan(
                ForegroundColorSpan(oldPriceColor),
                0,
                formattedOldPrice.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        .append(' ')
        .append(formattedNewPrice, StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        .apply {
            setSpan(
                ForegroundColorSpan(newPriceColor),
                formattedNewPrice.length + 1,
                length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
}