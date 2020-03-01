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

package de.r4md4c.gamedealz.feature.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.r4md4c.commonproviders.di.viewmodel.AssistedSavedStateViewModelFactory
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.di.ForApplication
import de.r4md4c.gamedealz.common.exhaustive
import de.r4md4c.gamedealz.common.launchWithCatching
import de.r4md4c.gamedealz.common.livedata.SingleLiveEvent
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.AddToWatchListArgument
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.ShopModel
import de.r4md4c.gamedealz.domain.model.Status
import de.r4md4c.gamedealz.domain.model.formatCurrency
import de.r4md4c.gamedealz.domain.usecase.AddToWatchListUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import timber.log.Timber
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

data class AddToWatchlistUIModel(
    val currentBest: String? = null,
    val availableStores: List<ShopModel>,
    val shopPrices: Map<ShopModel, PriceModel>,
    val toggledStoreMap: Map<ShopModel, Boolean>,
    val areAllStoresMarked: Boolean
)

@Suppress("TooManyFunctions")
class AddToWatchListViewModel @AssistedInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    plainDetails: GetPlainDetails,
    private val dispatchers: IDispatchers,
    private val getCurrentActiveRegionUseCase: GetCurrentActiveRegionUseCase,
    @ForApplication private val resourcesProvider: ResourcesProvider,
    private val addToWatchListUseCase: AddToWatchListUseCase
) : ViewModel() {

    sealed class UIEvent {
        object ShowLoading : UIEvent()
        object HideLoading : UIEvent()
        object Dismiss : UIEvent()
        data class ShowError(val errorString: String) : UIEvent()
        data class PriceError(val errorString: String) : UIEvent()
    }

    private val stateEventsChannel = Channel<StateEvent>(capacity = Channel.BUFFERED)

    private val _plainDetails by lazy { MutableLiveData<PlainDetailsModel>() }

    private val _addToWatchlistUIModel by lazy { MutableLiveData<AddToWatchlistUIModel>() }
    val addToWatchlistUIModel: LiveData<AddToWatchlistUIModel> by lazy {
        _addToWatchlistUIModel
    }

    private var activeRegion: ActiveRegion? = null

    private val _uiEvents by lazy { SingleLiveEvent<UIEvent>() }
    val uiEvents: LiveData<UIEvent> by lazy { _uiEvents }

    init {
        savedStateHandle.getLiveData<String>(KEY_PLAIN_ID)
            .asFlow()
            .onStart {
                activeRegion = getCurrentActiveRegionUseCase()
            }
            .flatMapConcat { plainDetails.invoke(TypeParameter(GetPlainDetails.Params(it))) }
            .onEach { storeResponse ->
                when (storeResponse.status) {
                    Status.SUCCESS -> {
                        val data = storeResponse.data!!
                        _uiEvents.postValue(UIEvent.HideLoading)
                        _plainDetails.postValue(data)
                    }
                    Status.ERROR -> _uiEvents.postValue(UIEvent.ShowError(storeResponse.message!!))
                    Status.LOADING -> _uiEvents.postValue(UIEvent.ShowLoading)
                }.exhaustive
            }
            .catch {
                Timber.e(it, "Error when getting plain details")
                _uiEvents.postValue(UIEvent.ShowError(it.localizedMessage))
            }
            .launchIn(viewModelScope)

        _plainDetails
            .asFlow()
            .map { details ->
                val shopPricesMap = details.shopPrices.mapValues { entry -> entry.value.priceModel }
                val availableStores = details.shopPrices.map { entry -> entry.key }
                val toggledStores = availableStores.associateWith { true }
                AddToWatchlistUIModel(
                    availableStores = availableStores,
                    currentBest = formatCurrentBestCurrencyModel(shopPricesMap.values),
                    shopPrices = shopPricesMap,
                    toggledStoreMap = toggledStores,
                    areAllStoresMarked = toggledStores.all { it.value }
                )
            }
            .flatMapConcat {
                stateEventsChannel.consumeAsFlow().scan(it, this::onReduce)
            }
            .flowOn(dispatchers.Default)
            .onEach { _addToWatchlistUIModel.postValue(it) }
            .launchIn(viewModelScope)
    }

    fun onSubmit(priceString: String) {
        val plainDetailsModel = _plainDetails.value
        if (priceString.isBlank() || plainDetailsModel == null) {
            _uiEvents.postValue(
                UIEvent.PriceError(resourcesProvider.getString(R.string.watchlist_error_empty_price))
            )
            return
        }
        val targetPrice = cleanUpAndValidatePrice(priceString) ?: return

        val uiModel = _addToWatchlistUIModel.value!!

        val leastToggledPrice = getSmallestToggledPrice(uiModel, plainDetailsModel)

        val selectedStores = getEnabledStores(uiModel)

        if (leastToggledPrice != null && targetPrice >= leastToggledPrice.newPrice) {
            val errorString = resourcesProvider.getString(
                R.string.watchlist_already_better_deal,
                leastToggledPrice.newPrice.formatCurrency(activeRegion!!.currency)!!,
                leastToggledPrice.shop.name
            )
            _uiEvents.postValue(UIEvent.PriceError(errorString))
        } else if (selectedStores.isEmpty()) {
            _uiEvents.postValue(UIEvent.ShowError(resourcesProvider.getString(R.string.watchlist_error_stores)))
        } else {
            doAddToWatchList(targetPrice, selectedStores, leastToggledPrice!!)
        }
    }

    private fun cleanUpAndValidatePrice(priceString: String): Float? =
        priceString.runCatching {
            val activeRegion = activeRegion!!
            val cleaned = cleanPriceText(this, activeRegion.currency, cleanSeparator = false)
            toBigDecimal(cleaned).toFloat()
        }.onFailure {
            val errorString = resourcesProvider.getString(
                R.string.watchlist_error_wrong_number_format
            )
            _uiEvents.postValue(UIEvent.PriceError(errorString))
        }.getOrNull()

    private fun getEnabledStores(uiModel: AddToWatchlistUIModel): List<ShopModel> {
        return uiModel.toggledStoreMap.asSequence()
            .filter { it.value }
            .map { it.key }
            .toList()
    }

    private fun getSmallestToggledPrice(
        uiModel: AddToWatchlistUIModel,
        plainDetailsModel: PlainDetailsModel
    ): PriceModel? =
        uiModel.toggledStoreMap.asSequence()
            .filter { it.value }
            .map { plainDetailsModel.shopPrices[it.key]!!.priceModel }
            .minBy {
                it.newPrice
            }

    fun onAllStoresChecked(isChecked: Boolean) {
        stateEventsChannel.offer(StateEvent.AllStoresToggleEvent(isChecked))
    }

    fun onStoreChipToggled(store: ShopModel, isChecked: Boolean) {
        stateEventsChannel.offer(StateEvent.StoreToggleEvent(store, isChecked))
    }

    fun formatPrice(editTextString: String): String? {
        val region = activeRegion ?: return null
        val cleanString = cleanPriceText(editTextString, region.currency)
        val parsed = toBigDecimal(cleanString)

        return activeRegion?.let { activeRegion ->
            numberFormatForCurrencyCode(activeRegion.currency).format(parsed)
        }
    }

    private suspend fun onReduce(
        state: AddToWatchlistUIModel,
        stateEvent: StateEvent
    ): AddToWatchlistUIModel =
        when (stateEvent) {
            is StateEvent.StoreToggleEvent -> {
                val newToggledStores = state.toggledStoreMap.mapValues { entry ->
                    if (stateEvent.shop == entry.key) stateEvent.isChecked else entry.value
                }
                val enabledStores = newToggledStores.filterValues { it }
                val pricesInEnabledStores =
                    state.shopPrices.filterKeys { shopModel -> shopModel in enabledStores }
                state.copy(
                    currentBest = formatCurrentBestCurrencyModel(pricesInEnabledStores.values),
                    toggledStoreMap = newToggledStores,
                    areAllStoresMarked = newToggledStores.all { it.value }
                )
            }
            is StateEvent.AllStoresToggleEvent -> {
                val newToggledStores = state.toggledStoreMap.mapValues { stateEvent.isToggled }
                val enabledStores = newToggledStores.filterValues { it }
                val pricesInEnabledStores =
                    state.shopPrices.filterKeys { shopModel -> shopModel in enabledStores }
                state.copy(
                    currentBest = formatCurrentBestCurrencyModel(pricesInEnabledStores.values),
                    areAllStoresMarked = stateEvent.isToggled,
                    toggledStoreMap = newToggledStores
                )
            }
        }

    private suspend fun formatCurrentBestCurrencyModel(priceModels: Iterable<PriceModel>): String? {
        val activeRegion = getCurrentActiveRegionUseCase()
        val smallestPriceModel = priceModels.minBy { it.newPrice }
        return smallestPriceModel?.newPrice?.formatCurrency(activeRegion.currency)
    }

    private fun doAddToWatchList(
        targetPrice: Float,
        selectedStores: List<ShopModel>,
        currentPrice: PriceModel
    ) {
        val plainId = savedStateHandle.get<String>(KEY_PLAIN_ID)!!
        val title = savedStateHandle.get<String>(KEY_TITLE)!!

        viewModelScope.launchWithCatching(dispatchers.Default, {
            val addToWatchListArgument =
                AddToWatchListArgument(
                    plainId = plainId,
                    title = title,
                    currentPrice = currentPrice.newPrice,
                    targetPrice = targetPrice,
                    currentStoreName = currentPrice.shop.name,
                    stores = selectedStores
                )

            addToWatchListUseCase.invoke(TypeParameter(addToWatchListArgument))

            _uiEvents.postValue(UIEvent.Dismiss)
        }) {
            Timber.e(it, "Failed to save game to watch list.")
            _uiEvents.postValue(
                UIEvent.ShowError(
                    resourcesProvider.getString(
                        R.string.watchlist_general_error,
                        title,
                        it.message ?: it.localizedMessage ?: ""
                    )
                )
            )
        }
    }

    private fun numberFormatForCurrencyCode(currencyModel: CurrencyModel): NumberFormat =
        getDecimalFormatForCurrencyModel(currencyModel)

    private fun cleanPriceText(
        text: String,
        currencyModel: CurrencyModel,
        cleanSeparator: Boolean = true
    ): String {
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
            .divide(BIG_DECIMAL_100, BigDecimal.ROUND_FLOOR)

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<AddToWatchListViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): AddToWatchListViewModel
    }

    private sealed class StateEvent {
        // Used for the onStart for the store toggling channel
        data class StoreToggleEvent(val shop: ShopModel, val isChecked: Boolean) : StateEvent()

        data class AllStoresToggleEvent(val isToggled: Boolean) : StateEvent()
    }

    private companion object {
        private const val KEY_PLAIN_ID = "plainId"
        private const val KEY_TITLE = "title"
        private val BIG_DECIMAL_100 = BigDecimal(100)
    }
}
