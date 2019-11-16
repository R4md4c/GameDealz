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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.text.DecimalFormat
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
            val activeRegion = activeRegion ?: return@runCatching null
            val cleaned = cleanPriceText(this, activeRegion.currency, cleanSeparator = false)
            toBigDecimal(cleaned).toFloat()
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

    fun formatCurrentBestCurrencyModel(priceModel: PriceModel): LiveData<String> {
        val currencyData = SingleLiveEvent<String>()
        uiScope.launch(dispatchers.IO) {
            val activeRegion = getCurrentActiveRegionUseCase()
            currencyData.postValue(priceModel.newPrice.formatCurrency(activeRegion.currency))
        }
        return currencyData
    }

    private fun doAddToWatchList(
        plainId: String,
        title: String,
        priceModel: PriceModel?,
        targetPrice: Float,
        selectedStores: List<StoreModel>
    ) {
        uiScope.launchWithCatching(dispatchers.Default, {
            val addToWatchListArgument =
                AddToWatchListArgument(
                    plainId = plainId,
                    title = title,
                    currentPrice = priceModel!!.newPrice,
                    targetPrice = targetPrice,
                    currentStoreName = priceModel.shop.name,
                    stores = selectedStores
                )

            addToWatchListUseCase.invoke(TypeParameter(addToWatchListArgument))

            _dismiss.postValue(Unit)
        }) {
            Timber.e(it, "Failed to save game to watch list.")
            _generalError.postValue(
                resourcesProvider.getString(
                    R.string.watchlist_general_error,
                    title,
                    it.message ?: it.localizedMessage ?: ""
                )
            )
        }
    }

    fun formatPrice(editTextString: String): String? {
        val region = activeRegion ?: return null
        val cleanString = cleanPriceText(editTextString, region.currency)
        val parsed = toBigDecimal(cleanString)

        return activeRegion?.let { activeRegion ->
            numberFormatForCurrencyCode(activeRegion.currency).format(parsed)
        }
    }

    private fun numberFormatForCurrencyCode(currencyModel: CurrencyModel): NumberFormat =
        getDecimalFormatForCurrencyModel(currencyModel)

    private fun cleanPriceText(text: String, currencyModel: CurrencyModel, cleanSeparator: Boolean = true): String {
        val itadSign = currencyModel.sign
        val decimalFormat = getDecimalFormatForCurrencyModel(currencyModel)
        val separator = decimalFormat.decimalFormatSymbols.decimalSeparator
        val groupingSeparator = decimalFormat.decimalFormatSymbols.groupingSeparator
        val currencySymbol = decimalFormat.decimalFormatSymbols.currencySymbol
        return text.replace(currencySymbol, "")
            .replace(itadSign, "")
            .replace(currencyModel.currencyCode, "")
            .replace("\\W+".toRegex(), "")
            .filter {
                when {
                    it.isDigit() -> true
                    cleanSeparator -> !(it == separator || it == groupingSeparator)
                    !cleanSeparator -> it == separator || it == groupingSeparator
                    else -> false
                }
            }.trim()
    }

    private fun getDecimalFormatForCurrencyModel(currencyModel: CurrencyModel): DecimalFormat {
        return (DecimalFormat.getCurrencyInstance(Locale.US) as DecimalFormat).apply {
            val currency = Currency.getInstance(currencyModel.currencyCode)
            this.currency = currency
            this.decimalFormatSymbols.currencySymbol = currencyModel.sign
        }
    }

    private fun toBigDecimal(cleanedPrice: String): BigDecimal =
        BigDecimal(cleanedPrice).setScale(2, BigDecimal.ROUND_FLOOR)
            .divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
}
