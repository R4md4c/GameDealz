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

package de.r4md4c.gamedealz.detail

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.launchWithCatching
import de.r4md4c.gamedealz.common.livedata.SingleLiveEvent
import de.r4md4c.gamedealz.common.navigator.Navigator
import de.r4md4c.gamedealz.common.state.Event
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.*
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import de.r4md4c.gamedealz.domain.usecase.IsGameAddedToWatchListUseCase
import de.r4md4c.gamedealz.domain.usecase.RemoveFromWatchlistUseCase
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Parcelize
data class DetailsViewModelState(val filterSelection: Int, val plainDetailsModel: PlainDetailsModel) : Parcelable

@Parcelize
data class PriceDetails(
    val priceModel: PriceModel,
    val shopModel: ShopModel,
    val historicalLowModel: HistoricalLowModel?,
    val currencyModel: CurrencyModel
) : Parcelable

data class GameInformation(val headerImage: String?, val shortDescription: String)

class DetailsViewModel(
    private val dispatchers: IDispatchers,
    private val navigator: Navigator,
    private val getPlainDetails: GetPlainDetails,
    private val stateMachineDelegate: StateMachineDelegate,
    private val isGameAddedToWatchListUseCase: IsGameAddedToWatchListUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase
) : AbstractViewModel(dispatchers) {

    private var loadedPlainDetailsModel: PlainDetailsModel? = null

    private val _screenshots by lazy { SingleLiveEvent<List<ScreenshotModel>>() }
    val screenshots: LiveData<List<ScreenshotModel>> by lazy { _screenshots }

    private val _prices by lazy { SingleLiveEvent<List<PriceDetails>>() }
    val prices: LiveData<List<PriceDetails>> by lazy {
        Transformations.switchMap(_filterItemChoice) {
            applyFilter(it)
        }
    }

    private val _gameInformation by lazy { SingleLiveEvent<GameInformation>() }
    val gameInformation: LiveData<GameInformation> by lazy { _gameInformation }

    private val _sideEffect by lazy { SingleLiveEvent<SideEffect>() }
    val sideEffect: LiveData<SideEffect> by lazy { _sideEffect }

    private val _filterItemChoice by lazy { MutableLiveData<Int>().apply { value = R.id.menu_item_current_best } }
    val currentFilterItemChoice: Int
        get() = _filterItemChoice.value!!

    private val _isAddedToWatchList by lazy { MutableLiveData<Boolean>().apply { value = false } }
    val isAddedToWatchList: LiveData<Boolean> by lazy { _isAddedToWatchList }

    init {
        stateMachineDelegate.onTransition { _sideEffect.postValue(it) }
    }

    fun onBuyButtonClick(buyUrl: String) {
        navigator.navigateToUrl(buyUrl)
    }

    fun onFilterChange(filterItemId: Int) {
        _filterItemChoice.postValue(filterItemId)
    }

    fun removeFromWatchlist(plainId: String) {
        uiScope.launchWithCatching(dispatchers.IO, {
            removeFromWatchlistUseCase(TypeParameter(plainId)).also {
                Timber.i("Removal of $plainId, from the Watchlist was: $it")
            }
        }) {
            Timber.e(it, "Failed to remove $plainId from the Watchlist.")
        }
    }

    fun loadIsAddedToWatchlist(plainId: String) {
        uiScope.launchWithCatching(dispatchers.IO, {

            isGameAddedToWatchListUseCase(TypeParameter(plainId)).run {
                _isAddedToWatchList.postValue(receiveOrNull() ?: false)
                consumeEach {
                    _isAddedToWatchList.postValue(it)
                }
            }

        }) {
            Timber.e(it, "Failed while watching watchee with plainId=$plainId")
        }
    }

    fun onRestoreState(detailsViewModelState: DetailsViewModelState) {
        uiScope.launch(dispatchers.Default) {
            postDetailsInfo(detailsViewModelState.plainDetailsModel)
            _filterItemChoice.postValue(detailsViewModelState.filterSelection)
        }
    }

    fun onSaveState(): DetailsViewModelState? =
        loadedPlainDetailsModel?.let { DetailsViewModelState(currentFilterItemChoice, it) }

    fun loadPlainDetails(plainId: String) = uiScope.launchWithCatching(dispatchers.IO, {

        stateMachineDelegate.transition(Event.OnLoadingStart)

        val details = getPlainDetails(TypeParameter(plainId))
        postDetailsInfo(details)

        stateMachineDelegate.transition(Event.OnLoadingEnded)

    }) {
        stateMachineDelegate.transition(Event.OnError(it))
    }

    private fun applyFilter(filterChoice: Int): LiveData<List<PriceDetails>> {
        uiScope.launchWithCatching(dispatchers.Default, {
            _prices.value?.sortedBy {
                when (filterChoice) {
                    R.id.menu_item_current_best -> it.priceModel.newPrice
                    R.id.menu_item_historical_low -> it.historicalLowModel?.price
                    else -> throw IllegalArgumentException("filterChoice is invalid")
                }
            }?.also { sorted -> _prices.postValue(sorted) }
        }) {
            stateMachineDelegate.transition(Event.OnError(it))
        }

        return _prices
    }

    private suspend fun postDetailsInfo(details: PlainDetailsModel) {
        details.shortDescription?.let {
            _gameInformation.postValue(GameInformation(details.headerImage, it))
        }

        if (details.screenshots.isNotEmpty()) {
            _screenshots.postValue(details.screenshots)
        }

        withContext(dispatchers.Default) {
            // TODO: Refactor this ugly piece of unreadable code.
            val value = details.shopPrices.map { it.key }
                .zip(details.shopPrices.map { it.value.priceModel })
                .zip(details.shopPrices.map { it.value.historicalLowModel })
                .map { PriceDetails(it.first.second, it.first.first, it.second, details.currencyModel) }
            _prices.postValue(value)
        }
        loadedPlainDetailsModel = details
    }
}