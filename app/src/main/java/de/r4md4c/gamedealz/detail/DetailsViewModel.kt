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
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.livedata.SingleLiveEvent
import de.r4md4c.gamedealz.common.navigator.Navigator
import de.r4md4c.gamedealz.common.state.Event
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.*
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val stateMachineDelegate: StateMachineDelegate
) : AbstractViewModel(dispatchers) {

    private var loadedPlainDetailsModel: PlainDetailsModel? = null

    private val _screenshots by lazy { SingleLiveEvent<List<ScreenshotModel>>() }
    val screenshots: LiveData<List<ScreenshotModel>> by lazy { _screenshots }

    private val _prices by lazy { SingleLiveEvent<List<PriceDetails>>() }
    val prices: LiveData<List<PriceDetails>> by lazy { _prices }

    private val _gameInformation by lazy { SingleLiveEvent<GameInformation>() }
    val gameInformation: LiveData<GameInformation> by lazy { _gameInformation }

    private val _sideEffect by lazy { SingleLiveEvent<SideEffect>() }
    val sideEffect: LiveData<SideEffect> by lazy { _sideEffect }

    init {
        stateMachineDelegate.onTransition { _sideEffect.postValue(it) }
    }

    fun onBuyButtonClick(buyUrl: String) {
        navigator.navigateToUrl(buyUrl)
    }

    fun onRestoreState(plainDetailsModel: PlainDetailsModel) {
        uiScope.launch(dispatchers.Default) {
            postDetailsInfo(plainDetailsModel)
        }
    }

    fun onSaveState(): PlainDetailsModel? = loadedPlainDetailsModel

    fun loadPlainDetails(plainId: String) = uiScope.launch(dispatchers.IO) {
        try {
            stateMachineDelegate.transition(Event.OnLoadingStart)

            val details = getPlainDetails(TypeParameter(plainId))
            postDetailsInfo(details)

            stateMachineDelegate.transition(Event.OnLoadingEnded)
        } catch (e: Exception) {
            stateMachineDelegate.transition(Event.OnError(e))
        }
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
            details.shopPrices.map { it.key }
                .zip(details.shopPrices.map { it.value.priceModel })
                .zip(details.shopPrices.map { it.value.historicalLowModel })
                .map { PriceDetails(it.first.second, it.first.first, it.second, details.currencyModel) }
                .run { _prices.postValue(this) }
        }
        loadedPlainDetailsModel = details
    }
}