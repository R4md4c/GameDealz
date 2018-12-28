package de.r4md4c.gamedealz.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.r4md4c.gamedealz.common.navigator.Navigator
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
    private val getPlainDetails: GetPlainDetails
) : AbstractViewModel() {

    private val _screenshots by lazy { MutableLiveData<List<ScreenshotModel>>() }
    val screenshots: LiveData<List<ScreenshotModel>> by lazy { _screenshots }

    private val _prices by lazy { MutableLiveData<List<PriceDetails>>() }
    val prices: LiveData<List<PriceDetails>> by lazy { _prices }

    private val _gameInformation by lazy { MutableLiveData<GameInformation>() }
    val gameInformation: LiveData<GameInformation> by lazy { _gameInformation }

    fun onBuyButtonClick(buyUrl: String) {
        navigator.navigateToUrl(buyUrl)
    }

    fun loadPlainDetails(plainId: String) = uiScope.launch(IO) {
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
    }
}