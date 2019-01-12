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
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.launchWithCatching
import de.r4md4c.gamedealz.common.livedata.SingleLiveEvent
import de.r4md4c.gamedealz.common.state.Event
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ManageWatchlistModel
import de.r4md4c.gamedealz.domain.usecase.CheckPriceThresholdUseCase
import de.r4md4c.gamedealz.domain.usecase.GetLatestWatchlistCheckDate
import de.r4md4c.gamedealz.domain.usecase.GetWatchlistToManageUseCase
import de.r4md4c.gamedealz.domain.usecase.RemoveWatcheesUseCase
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ManageWatchlistViewModel(
    private val dispatchers: IDispatchers,
    private val getWatchlistUseCase: GetWatchlistToManageUseCase,
    private val getLatestWatchlistCheckDate: GetLatestWatchlistCheckDate,
    private val removeWatcheesUseCase: RemoveWatcheesUseCase,
    private val stateMachineDelegate: StateMachineDelegate,
    private val dateFormatter: DateFormatter,
    private val checkPricesUseCase: CheckPriceThresholdUseCase,
    private val resourcesProvider: ResourcesProvider
) : AbstractViewModel(dispatchers) {

    private val _watchlistLiveData by lazy { MutableLiveData<List<ManageWatchlistModel>>() }
    val watchlistLiveData: LiveData<List<ManageWatchlistModel>> by lazy { _watchlistLiveData }

    private val _lastCheckDate by lazy { MutableLiveData<String>() }
    val lastCheckDate: LiveData<String> by lazy { _lastCheckDate }

    private val _sideEffects by lazy { SingleLiveEvent<SideEffect>() }
    val sideEffects: LiveData<SideEffect> by lazy { _sideEffects }

    init {
        stateMachineDelegate.onTransition { _sideEffects.postValue(it) }
    }

    fun init() {
        observeWatchlistData()
        observeLatestCheckDate()
    }

    fun onRemoveWatchee(tobeRemoved: List<ManageWatchlistModel>) {
        uiScope.launchWithCatching(dispatchers.IO, {
            val count = removeWatcheesUseCase(TypeParameter(tobeRemoved.map { it.watcheeModel }))
            Timber.d("Remove $count items from Watchlist.")
        }) {
            Timber.e(it, "Failed to remove watchees")
        }
    }

    fun onSwipeToRefresh() {
        uiScope.launchWithCatching(dispatchers.Main, {
            stateMachineDelegate.transition(Event.OnLoadingStart)
            withContext(dispatchers.IO) { checkPricesUseCase() }
            stateMachineDelegate.transition(Event.OnLoadingEnded)
        }) {
            stateMachineDelegate.transition(Event.OnError(it))
            Timber.e(it, "Failed to do a Swipe to refresh")
        }
    }

    private fun observeWatchlistData() {
        uiScope.launchWithCatching(dispatchers.IO, {
            getWatchlistUseCase().consumeEach {
                _watchlistLiveData.postValue(it)
                if (it.isEmpty()) {
                    stateMachineDelegate.transition(Event.OnShowEmpty)
                }
            }
        }) {
            Timber.e(it, "Failed to observeWatchlistData")
        }
    }

    private fun observeLatestCheckDate() {
        uiScope.launchWithCatching(dispatchers.IO, {
            getLatestWatchlistCheckDate().filter { it > 0 }.consumeEach {
                val formattedTimeSpan = dateFormatter.getRelativeTimeSpanString(TimeUnit.SECONDS.toMillis(it))
                val lastCheckDate =
                    resourcesProvider.getString(R.string.manage_watch_list_last_checked, formattedTimeSpan)
                _lastCheckDate.postValue(lastCheckDate)
            }
        }) {
            Timber.e(it, "Failed to observeLatestCheckDate")
        }
    }

}
