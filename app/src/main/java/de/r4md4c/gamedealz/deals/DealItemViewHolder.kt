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

package de.r4md4c.gamedealz.deals

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.newAndOldPriceSpan
import de.r4md4c.gamedealz.domain.model.DealModel
import kotlinx.android.synthetic.main.layout_deal_item.view.*

class DealItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val newPriceColor by lazy { ContextCompat.getColor(itemView.context, R.color.newPriceColor) }
    private val oldPriceColor by lazy { ContextCompat.getColor(itemView.context, R.color.oldPriceColor) }

    fun onBind(dealModel: DealModel?, clickListener: (DealModel) -> Unit) {
        with(itemView) {
            setOnClickListener { dealModel?.let { clickListener(dealModel) } }

            name.text = dealModel?.title
            price.text = dealModel?.newAndOldPriceSpan(newPriceColor, oldPriceColor)
            stores.text = dealModel?.storeAndTimeSpan(itemView.context)

            GlideApp.with(image)
                .load(dealModel?.urls?.imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .fitCenter()
                .into(image)
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