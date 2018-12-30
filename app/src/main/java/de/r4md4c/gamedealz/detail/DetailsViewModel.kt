package de.r4md4c.gamedealz.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.r4md4c.gamedealz.common.navigator.Navigator
import de.r4md4c.gamedealz.common.state.Event
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.*
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PriceDetails(
    val priceModel: PriceModel,
    val shopModel: ShopModel,
    val historicalLowModel: HistoricalLowModel?,
    val currencyModel: CurrencyModel
)

data class GameInformation(val headerImage: String?, val shortDescription: String)

class DetailsViewModel(
    private val navigator: Navigator,
    private val getPlainDetails: GetPlainDetails,
    private val stateMachineDelegate: StateMachineDelegate
) : AbstractViewModel() {

    private val _screenshots by lazy { MutableLiveData<List<ScreenshotModel>>() }
    val screenshots: LiveData<List<ScreenshotModel>> by lazy { _screenshots }

    private val _prices by lazy { MutableLiveData<List<PriceDetails>>() }
    val prices: LiveData<List<PriceDetails>> by lazy { _prices }

    private val _gameInformation by lazy { MutableLiveData<GameInformation>() }
    val gameInformation: LiveData<GameInformation> by lazy { _gameInformation }

    private val _sideEffect by lazy { MutableLiveData<SideEffect>() }
    val sideEffect: LiveData<SideEffect> by lazy { _sideEffect }

    init {
        stateMachineDelegate.onTransition { _sideEffect.postValue(it) }
    }

    fun onBuyButtonClick(buyUrl: String) {
        navigator.navigateToUrl(buyUrl)
    }

    fun loadPlainDetails(plainId: String) = uiScope.launch(IO) {
        try {
            stateMachineDelegate.transition(Event.OnLoadingStart)
            val details = getPlainDetails(TypeParameter(plainId))
            details.shortDescription?.let {
                _gameInformation.postValue(GameInformation(details.headerImage, it))
            }

            if (details.screenshots.isNotEmpty()) {
                _screenshots.postValue(details.screenshots)
            }

            withContext(Dispatchers.Default) {
                // TODO: Refactor this ugly piece of unreadable code.
                details.shopPrices.map { it.key }
                    .zip(details.shopPrices.map { it.value.first })
                    .zip(details.shopPrices.map { it.value.second })
                    .map { PriceDetails(it.first.second, it.first.first, it.second, details.currencyModel) }
                    .run { _prices.postValue(this) }
            }
            stateMachineDelegate.transition(Event.OnLoadingEnded)
        } catch (e: Exception) {
            stateMachineDelegate.transition(Event.OnError(e))
        }
    }
}