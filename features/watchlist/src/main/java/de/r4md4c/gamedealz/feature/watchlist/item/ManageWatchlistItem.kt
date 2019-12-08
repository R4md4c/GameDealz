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

package de.r4md4c.gamedealz.feature.watchlist.item

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.WorkerThread
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.domain.model.ManageWatchlistModel
import de.r4md4c.gamedealz.domain.model.formatCurrency
import de.r4md4c.gamedealz.feature.watchlist.R
import kotlinx.android.synthetic.main.layout_manage_watchlist_item.view.*

data class ManageWatchlistItem(
    private val watchModelTitle: CharSequence,
    private val watchModelTargetPrice: CharSequence,
    private val watchModelCurrentPrice: CharSequence,
    private val hasNotifciation: Boolean
) : AbstractItem<ManageWatchlistItem, ManageWatchlistItem.ViewHolder>() {

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        with(holder.itemView) {
            title.text = watchModelTitle
            targetPrice.text = watchModelTargetPrice
            currentPrice.text = watchModelCurrentPrice
            notification.isVisible = hasNotifciation
        }
    }

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_manage_watchlist_item

    override fun getViewHolder(v: View): ViewHolder =
        ViewHolder(
            v
        )

    override fun getLayoutRes(): Int = R.layout.layout_manage_watchlist_item

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

@WorkerThread
fun ManageWatchlistModel.toManageWatchlistItem(
    resourcesProvider: ResourcesProvider
): ManageWatchlistItem? {
    val formattedTargetPrice = watcheeModel.targetPrice.formatCurrency(currencyModel) ?: return null
    val formattedCurrentPrice = watcheeModel.lastFetchedPrice.formatCurrency(currencyModel) ?: return null
    val targetPriceString = buildSpannedString {
        val targetPriceString = resourcesProvider.getString(R.string.manage_watch_list_target_price)
        if (hasNotification) {
            bold {
                append(targetPriceString)
            }
        } else {
            append(targetPriceString)
        }
    }
    val lastFetchedPrice = buildSpannedString {
        val lastFetchedPriceString = resourcesProvider.getString(R.string.managed_watch_list_current_price)
        if (hasNotification) {
            bold { append(lastFetchedPriceString) }
        } else {
            append(lastFetchedPriceString)
        }
    }

    return ManageWatchlistItem(
        watcheeModel.title,
        targetPriceString.concatWithPrice(formattedTargetPrice, resourcesProvider),
        lastFetchedPrice.concatWithLastChecked(
            formattedCurrentPrice,
            watcheeModel.lastFetchedStoreName,
            hasNotification,
            resourcesProvider
        ),
        hasNotification
    )
        .withIdentifier(watcheeModel.id ?: return null)
}

private fun CharSequence.concatWithLastChecked(
    formattedPrice: String,
    storeName: String,
    hasNotification: Boolean,
    resourcesProvider: ResourcesProvider
): CharSequence {
    val currentString = this
    return buildSpannedString {
        append(currentString)
        append(' ')
        val storeNameString = " ${resourcesProvider.getString(R.string.on)} $storeName"
        bold {
            color(resourcesProvider.getColor(R.color.newPriceColor)) {
                append(formattedPrice)
            }
        }
        if (hasNotification) {
            bold { append(storeNameString) }
        } else {
            append(storeNameString)
        }
    }
}

private fun CharSequence.concatWithPrice(
    formattedPrice: String,

    resourcesProvider: ResourcesProvider
): CharSequence {
    val currentString = this
    return buildSpannedString {
        append(currentString)
        append(' ')
        bold {
            color(resourcesProvider.getColor(R.color.newPriceColor)) {
                append(formattedPrice)
            }
        }
    }
}
