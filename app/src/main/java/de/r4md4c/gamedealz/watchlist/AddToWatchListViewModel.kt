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
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import kotlinx.coroutines.channels.first
import timber.log.Timber
import java.math.BigDecimal
import java.util.*

class AddToWatchListViewModel(
    private val dispatchers: IDispatchers,
    private val resourcesProvider: ResourcesProvider,
    private val getCurrentActiveRegionUseCase: GetCurrentActiveRegionUseCase,
    private val getStoresUseCase: GetStoresUseCase
) : AbstractViewModel(dispatchers) {

    private val _availableStores by lazy { MutableLiveData<List<StoreModel>>() }
    private var activeRegion: ActiveRegion? = null

    private val _emptyPriceError by lazy { SingleLiveEvent<String>() }
    val emptyPriceError by lazy { _emptyPriceError }
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

    fun onSubmit(priceString: String, title: String, plainId: String, priceModel: PriceModel) {
        if (priceString.isBlank()) {
            emptyPriceError.postValue(resourcesProvider.getString(R.string.watchlist_error_empty_price))
            return
        }
        val targetPrice = priceString.runCatching {
            replace("[${activeRegion?.currency?.toCurrencySymbol()}]".toRegex(), "").toFloat()
        }
            .onFailure { emptyPriceError.postValue(resourcesProvider.getString(R.string.watchlist_error_wrong_number_format)) }
            .getOrNull() ?: return

        _dismiss.postValue(Unit)
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