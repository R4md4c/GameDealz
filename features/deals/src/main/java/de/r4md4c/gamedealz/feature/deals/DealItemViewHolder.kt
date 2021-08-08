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

package de.r4md4c.gamedealz.feature.deals

import android.content.Context
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.commonproviders.extensions.resolveThemeColor
import de.r4md4c.gamedealz.common.image.GlideApp
import de.r4md4c.gamedealz.feature.deals.databinding.LayoutDealItemBinding
import de.r4md4c.gamedealz.feature.deals.model.DealRenderModel

class DealItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val binding = LayoutDealItemBinding.bind(itemView)

    init {
        binding.price.setSpannableFactory(PriceTextSpannableFactory())
    }

    fun onBind(dealModel: DealRenderModel?, clickListener: (DealRenderModel) -> Unit) {
        binding.root.setOnClickListener { dealModel?.let { clickListener(dealModel) } }

        binding.name.text = dealModel?.title
        binding.price.setText(dealModel?.newPrice, TextView.BufferType.SPANNABLE)
        replaceNewPriceForegroundColorSpanWithColorFromTheme(
            binding.root.context,
            binding.price.text as Spannable
        )

        binding.stores.text = dealModel?.storesAndTime

        GlideApp.with(binding.image)
            .load(dealModel?.imageUrl)
            .placeholder(R.drawable.ic_placeholder)
            .fitCenter()
            .into(binding.image)
    }

    private fun replaceNewPriceForegroundColorSpanWithColorFromTheme(context: Context, spannableText: Spannable) {
        spannableText.getSpans(0, spannableText.length, ForegroundColorSpan::class.java)[1].let {
            val spanStart = spannableText.getSpanStart(it)
            val spanEnd = spannableText.getSpanEnd(it)
            val spanFlags = spannableText.getSpanFlags(it)

            spannableText.removeSpan(it)
            spannableText.setSpan(
                ForegroundColorSpan(context.resolveThemeColor(R.attr.new_price_text_color)),
                spanStart,
                spanEnd,
                spanFlags
            )
        }
    }

    private class PriceTextSpannableFactory : Spannable.Factory() {
        override fun newSpannable(source: CharSequence?): Spannable {
            return source as Spannable
        }
    }
}
