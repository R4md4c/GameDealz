package de.r4md4c.gamedealz.deals

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.model.formatCurrency
import de.r4md4c.gamedealz.utils.image.GlideApp
import kotlinx.android.synthetic.main.layout_deal_item.view.*

class DealItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val discountColor by lazy { ContextCompat.getColor(itemView.context, R.color.discountColor) }
    private val oldPriceColor by lazy { ContextCompat.getColor(itemView.context, R.color.oldPriceColor) }

    fun onBind(dealModel: DealModel?) {

        with(itemView) {
            name.text = dealModel?.title
            price.text = dealModel?.priceSpan()
            stores.text = dealModel?.storeAndTimeSpan(itemView.context)

            GlideApp.with(image)
                .load(dealModel?.urls?.imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(image)
        }
    }

    private fun DealModel.priceSpan(): Spannable? {
        val oldPrice = oldPrice.formatCurrency(currencyModel) ?: return null
        val newPrice = newPrice.formatCurrency(currencyModel) ?: return null

        return SpannableStringBuilder()
            .append(oldPrice, StrikethroughSpan(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            .apply {
                setSpan(
                    ForegroundColorSpan(oldPriceColor),
                    0,
                    oldPrice.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            .append(' ')
            .append(newPrice, StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            .apply {
                setSpan(
                    ForegroundColorSpan(discountColor),
                    oldPrice.length + 1,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
    }

    private fun DealModel.storeAndTimeSpan(context: Context): Spannable? {
        val timestampString = DateUtils.getRelativeTimeSpanString(added * 1000)

        return SpannableStringBuilder()
            .append(context.getString(R.string.on))
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
            .append(context.getString(R.string.since))
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
}