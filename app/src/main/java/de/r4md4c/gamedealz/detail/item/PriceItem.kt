package de.r4md4c.gamedealz.detail.item

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.newAndOldPriceSpan
import de.r4md4c.gamedealz.detail.PriceDetails
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.model.HistoricalLowModel
import de.r4md4c.gamedealz.domain.model.formatCurrency
import kotlinx.android.synthetic.main.layout_detail_prices_item.view.*
import java.util.concurrent.TimeUnit

class PriceItem(
    private val priceDetails: PriceDetails,
    private val resourcesProvider: ResourcesProvider,
    private val dateFormatter: DateFormatter,
    private val onBuyClick: (PriceDetails) -> Unit
) : AbstractItem<PriceItem, PriceItem.ViewHolder>() {
    private val newPriceColor by lazy { resourcesProvider.getColor(R.color.newPriceColor) }
    private val oldPriceColor by lazy { resourcesProvider.getColor(R.color.oldPriceColor) }

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_detail_prices_item

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    override fun getLayoutRes(): Int = R.layout.layout_detail_prices_item

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        with(holder.itemView) {
            currentBest.text = currentBestString(priceDetails)
            historicalLow.text =
                    priceDetails.historicalLowModel?.let { historicalLowString(it, priceDetails.currencyModel) }
            shop.text = priceDetails.shopModel.name
            setOnClickListener { onBuyClick(priceDetails) }
        }
    }

    private fun currentBestString(priceDetails: PriceDetails): CharSequence {
        val currentBestPrice =
            priceDetails.priceModel.newAndOldPriceSpan(priceDetails.currencyModel, newPriceColor, oldPriceColor)
        return TextUtils.concat(resourcesProvider.getString(R.string.current_best), " ", currentBestPrice)
    }

    private fun historicalLowString(
        historicalLowModel: HistoricalLowModel,
        currencyModel: CurrencyModel
    ): CharSequence {
        return TextUtils.concat(
            resourcesProvider.getString(R.string.historical_low),
            " ",
            SpannableString(historicalLowModel.price.formatCurrency(currencyModel))
                .apply {
                    setSpan(ForegroundColorSpan(newPriceColor), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(StyleSpan(Typeface.BOLD), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                },
            " ",
            resourcesProvider.getString(R.string.on),
            " ",
            SpannableString(
                dateFormatter.formateDateTime(
                    TimeUnit.SECONDS.toMillis(historicalLowModel.added),
                    DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_YEAR
                )
            )
                .apply { setSpan(StyleSpan(Typeface.BOLD_ITALIC), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) })
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}