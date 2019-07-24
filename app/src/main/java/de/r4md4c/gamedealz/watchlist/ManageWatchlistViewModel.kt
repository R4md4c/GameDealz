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
import de.r4md4c.commonproviders.notification.Notifier
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.launchWithCatching
import de.r4md4c.gamedealz.common.livedata.SingleLiveEvent
import de.r4md4c.gamedealz.common.state.Event
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ManageWatchlistModel
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import de.r4md4c.gamedealz.domain.usecase.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class ManageWatchlistViewModel(
    private val dispatchers: IDispatchers,
    private val getWatchlistUseCase: GetWatchlistToManageUseCase,
    private val getLatestWatchlistCheckDate: GetLatestWatchlistCheckDate,
    private val removeWatcheesUseCase: RemoveWatcheesUseCase,
    private val stateMachineDelegate: StateMachineDelegate,
    private val dateFormatter: DateFormatter,
    private val checkPricesUseCase: CheckPriceThresholdUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val notifier: Notifier<WatcheeNotificationModel>
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

    private val removedItems = Collections.synchronizedSet(mutableSetOf<ManageWatchlistModel>())

    fun init() {
        observeWatchlistData()
        observeLatestCheckDate()
    }

    fun onRemoveWatchee(tobeRemoved: List<ManageWatchlistModel>) {
        uiScope.launchWithCatching(dispatchers.IO, {
            removeWatcheesUseCase(TypeParameter(tobeRemoved.map { it.watcheeModel }))
            removedItems -= tobeRemoved
        }) {
            Timber.e(it, "Failed to remove watchees")
        }
    }

    fun onItemSwiped(model: ManageWatchlistModel) {
        removedItems += model
    }

    fun onItemUndone(model: ManageWatchlistModel) {
        removedItems -= model
        uiScope.launchWithCatching(dispatchers.IO, { refreshWatchlist() }) {
            Timber.e(it, "Failed to refresh watchlist after undoing operation.%")
        }
    }

    fun onSwipeToRefresh() {
        uiScope.launchWithCatching(dispatchers.Main, {
            stateMachineDelegate.transition(Event.OnLoadingStart)
            val notificationModels = withContext(dispatchers.IO) { checkPricesUseCase() }
            if (notificationModels.isNotEmpty()) {
                notifier.notify(notificationModels)
                refreshWatchlist()
            }
            stateMachineDelegate.transition(Event.OnLoadingEnded)
            if (notificationModels.isEmpty() && _watchlistLiveData.value?.isEmpty() == true) {
                stateMachineDelegate.transition(Event.OnShowEmpty)
            }
        }) {
            stateMachineDelegate.transition(Event.OnError(it))
            Timber.e(it, "Failed to do a Swipe to refresh")
        }
    }

    fun markAsRead(model: ManageWatchlistModel) {
        uiScope.launchWithCatching(dispatchers.IO, {
            markNotificationAsReadUseCase(TypeParameter(model.watcheeModel))
            refreshWatchlist()
        }) {
            Timber.e(it, "Failed to mark alert as read.")
        }
    }

    private fun observeWatchlistData() {
        uiScope.launchWithCatching(dispatchers.IO, {
            getWatchlistUseCase().consumeAsFlow()
                .map { it - removedItems }
                .collect { postWatchlist(it) }
        }) {
            Timber.e(it, "Failed to observeWatchlistData")
        }
    }

    private fun observeLatestCheckDate() {
        uiScope.launchWithCatching(dispatchers.IO, {
            getLatestWatchlistCheckDate().filter { it > 0 }.consumeEach {
                val formattedTimeSpan = dateFormatter.getRelativeTimeSpanString(TimeUnit.SECONDS.toMillis(it))
                _lastCheckDate.postValue(formattedTimeSpan)
            }
        }) {
            Timber.e(it, "Failed to observeLatestCheckDate")
        }
    }

    suspend fun refreshWatchlist() {
        getWatchlistUseCase().consumeAsFlow().first().also { postWatchlist(it) }
    }

    private fun postWatchlist(manageWatchlistModel: List<ManageWatchlistModel>) {
        Timber.d("Posting $manageWatchlistModel")
        _watchlistLiveData.postValue(manageWatchlistModel)
        if (manageWatchlistModel.isEmpty()) {
            stateMachineDelegate.transition(Event.OnShowEmpty)
        } else {
            stateMachineDelegate.transition(Event.OnLoadingEnded)
        }
    }
}
