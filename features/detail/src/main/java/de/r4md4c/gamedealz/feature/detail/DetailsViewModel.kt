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

package de.r4md4c.gamedealz.feature.detail

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.di.ForApplication
import de.r4md4c.gamedealz.common.launchWithCatching
import de.r4md4c.gamedealz.common.livedata.SingleLiveEvent
import de.r4md4c.gamedealz.common.state.Event
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.model.HistoricalLowModel
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.ScreenshotModel
import de.r4md4c.gamedealz.domain.model.ShopModel
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import de.r4md4c.gamedealz.domain.usecase.IsGameAddedToWatchListUseCase
import de.r4md4c.gamedealz.domain.usecase.RemoveFromWatchlistUseCase
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

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

class DetailsViewModel @Inject constructor(
    private val dispatchers: IDispatchers,
    private val getPlainDetails: GetPlainDetails,
    private val stateMachineDelegate: StateMachineDelegate,
    private val isGameAddedToWatchListUseCase: IsGameAddedToWatchListUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    @ForApplication private val resourcesProvider: ResourcesProvider
) : ViewModel() {

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

    fun onFilterChange(filterItemId: Int) {
        _filterItemChoice.postValue(filterItemId)
    }

    suspend fun removeFromWatchlist(plainId: String) = withContext(dispatchers.IO) {
        removeFromWatchlistUseCase(TypeParameter(plainId)).also {
            Timber.i("Removal of $plainId, from the Watchlist was: $it")
        }
    }

    fun loadIsAddedToWatchlist(plainId: String) {
        viewModelScope.launchWithCatching(dispatchers.IO, {

            isGameAddedToWatchListUseCase(TypeParameter(plainId)).run {
                _isAddedToWatchList.postValue(first())
                collect {
                    _isAddedToWatchList.postValue(it)
                }
            }
        }) {
            Timber.e(it, "Failed while watching watchee with plainId=$plainId")
        }
    }

    fun onRestoreState(detailsViewModelState: DetailsViewModelState) {
        viewModelScope.launch(dispatchers.Default) {
            postDetailsInfo(detailsViewModelState.plainDetailsModel)
            _filterItemChoice.postValue(detailsViewModelState.filterSelection)
        }
    }

    fun onSaveState(): DetailsViewModelState? =
        loadedPlainDetailsModel?.let {
            DetailsViewModelState(
                currentFilterItemChoice,
                it
            )
        }

    fun getRestOfScreenshots(): List<ScreenshotModel> {
        if (_screenshots.value == null) {
            return emptyList()
        }

        val spanCount = resourcesProvider.getInteger(R.integer.screenshots_span_count)
        return _screenshots.value!!.takeLast(_screenshots.value!!.size - spanCount)
    }

    fun loadPlainDetails(plainId: String) = viewModelScope.launchWithCatching(dispatchers.IO, {

        stateMachineDelegate.transition(Event.OnLoadingStart)

        val details = getPlainDetails(TypeParameter(plainId))
        postDetailsInfo(details)

        stateMachineDelegate.transition(Event.OnLoadingEnded)
    }) {
        stateMachineDelegate.transition(Event.OnError(it))
    }

    private fun applyFilter(filterChoice: Int): LiveData<List<PriceDetails>> {
        viewModelScope.launchWithCatching(dispatchers.Default, {
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
            _gameInformation.postValue(
                GameInformation(
                    details.headerImage,
                    it
                )
            )
        }

        if (details.screenshots.isNotEmpty()) {
            _screenshots.postValue(details.screenshots)
        }

        withContext(dispatchers.Default) {
            // TODO: Refactor this ugly piece of unreadable code.
            val value = details.shopPrices.map { it.key }
                .zip(details.shopPrices.map { it.value.priceModel })
                .zip(details.shopPrices.map { it.value.historicalLowModel })
                .map {
                    PriceDetails(
                        it.first.second,
                        it.first.first,
                        it.second,
                        details.currencyModel
                    )
                }
            _prices.postValue(value)
        }
        loadedPlainDetailsModel = details
    }
}
