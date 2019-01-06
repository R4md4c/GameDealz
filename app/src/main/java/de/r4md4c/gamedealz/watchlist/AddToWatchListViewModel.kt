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

package de.r4md4c.gamedealz.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.launchWithCatching
import de.r4md4c.gamedealz.common.livedata.SingleLiveEvent
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.*
import de.r4md4c.gamedealz.domain.usecase.AddToWatchListUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import kotlinx.coroutines.channels.first
import timber.log.Timber
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

class AddToWatchListViewModel(
    private val dispatchers: IDispatchers,
    private val resourcesProvider: ResourcesProvider,
    private val getCurrentActiveRegionUseCase: GetCurrentActiveRegionUseCase,
    private val getStoresUseCase: GetStoresUseCase,
    private val addToWatchListUseCase: AddToWatchListUseCase
) : AbstractViewModel(dispatchers) {

    private val _availableStores by lazy { MutableLiveData<List<StoreModel>>() }
    private var activeRegion: ActiveRegion? = null

    private val _emptyPriceError by lazy { SingleLiveEvent<String>() }
    val emptyPriceError: LiveData<String> by lazy { _emptyPriceError }

    private val _generalError by lazy { SingleLiveEvent<String>() }
    val generalError: LiveData<String> by lazy { _generalError }

    private val _dismiss by lazy { SingleLiveEvent<Unit>() }
    val dismiss: LiveData<Unit> by lazy { _dismiss }

    fun loadStores(): LiveData<List<StoreModel>> {
        uiScope.launchWithCatching(dispatchers.IO, {
            this.activeRegion = getCurrentActiveRegionUseCase()
            val stores = getStoresUseCase.invoke(TypeParameter(activeRegion!!)).first()
            _availableStores.postValue(stores)
        }) {
            Timber.e(it, "Failed to load the stores")
        }

        return _availableStores
    }

    fun onSubmit(
        priceString: String,
        title: String,
        plainId: String,
        priceModel: PriceModel?,
        selectedStores: List<StoreModel>
    ) {
        if (priceString.isBlank()) {
            _emptyPriceError.postValue(resourcesProvider.getString(R.string.watchlist_error_empty_price))
            return
        }
        val targetPrice = priceString.runCatching {

            val cleaned = replace("[${activeRegion?.currency?.toCurrencySymbol()}]".toRegex(), "")
            val numberFormat = NumberFormat.getNumberInstance()
            numberFormat.parse(cleaned).toFloat()
        }
            .onFailure { _emptyPriceError.postValue(resourcesProvider.getString(R.string.watchlist_error_wrong_number_format)) }
            .getOrNull() ?: return

        if (priceModel != null && targetPrice >= priceModel.newPrice) {
            _emptyPriceError.postValue(
                resourcesProvider.getString(
                    R.string.watchlist_already_better_deal,
                    priceModel.newPrice.formatCurrency(activeRegion!!.currency)!!, priceModel.shop.name
                )
            )
            return
        }

        if (selectedStores.isEmpty()) {
            _generalError.postValue(resourcesProvider.getString(R.string.watchlist_error_stores))
            return
        }

        doAddToWatchList(plainId, title, priceModel, targetPrice, selectedStores)

    }

    private fun doAddToWatchList(
        plainId: String,
        title: String,
        priceModel: PriceModel?,
        targetPrice: Float,
        selectedStores: List<StoreModel>
    ) {
        uiScope.launchWithCatching(dispatchers.Default, {
            val watcheeModel = WatcheeModel(
                plainId = plainId,
                title = title,
                currentPrice = priceModel!!.newPrice,
                targetPrice = targetPrice
            )
            val addToWatchListArgument = AddToWatchListArgument(watcheeModel, selectedStores)

            addToWatchListUseCase(TypeParameter(addToWatchListArgument))

            _dismiss.postValue(Unit)
        }) {
            Timber.e(it, "Failed to save game to watch list.")
            _generalError.postValue(
                resourcesProvider.getString(
                    R.string.watchlist_general_error,
                    title,
                    it.localizedMessage
                )
            )
        }
    }

    fun formatPrice(editTextString: String): String? {
        val symbol = activeRegion?.currency?.toCurrencySymbol() ?: return null

        val cleanString = editTextString.replace("[$symbol,.]".toRegex(), "")
        val parsed = BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR)
            .divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
        return activeRegion?.let { parsed.toFloat().formatCurrency(it.currency) }
    }

    private fun CurrencyModel.toCurrencySymbol(): String =
        Currency.getInstance(currencyCode).run { symbol }

}