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

package de.r4md4c.gamedealz.feature.home

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.r4md4c.commonproviders.appcompat.NightMode
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.di.ForApplication
import de.r4md4c.gamedealz.common.livedata.SingleLiveEvent
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.displayName
import de.r4md4c.gamedealz.domain.usecase.GetAlertsCountUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.domain.usecase.OnNightModeChangeUseCase
import de.r4md4c.gamedealz.domain.usecase.ToggleNightModeUseCase
import de.r4md4c.gamedealz.feature.home.state.DrawerItem
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.feature.home.state.action.ViewAction
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val dispatchers: IDispatchers,
    private val getCurrentActiveRegion: GetCurrentActiveRegionUseCase,
    private val onActiveRegionChange: OnCurrentActiveRegionReactiveUseCase,
    private val priceAlertsCountUseCase: GetAlertsCountUseCase,
    private val toggleNightModeUseCase: ToggleNightModeUseCase,
    private val onNightModeChangeUseCase: OnNightModeChangeUseCase,
    @ForApplication private val resourcesProvider: ResourcesProvider
) : ViewModel() {

    private val _currentRegion by lazy { MutableLiveData<ActiveRegion>() }
    val currentRegion: LiveData<ActiveRegion> by lazy { _currentRegion }

    private val _regionsLoading by lazy { MutableLiveData<Boolean>() }
    val regionsLoading: LiveData<Boolean> by lazy { _regionsLoading }

    private val _openRegionSelectionDialog by lazy { SingleLiveEvent<ActiveRegion>() }
    val openRegionSelectionDialog: LiveData<ActiveRegion> by lazy { _openRegionSelectionDialog }

    private val _closeDrawer by lazy { MutableLiveData<Unit>() }
    val closeDrawer: LiveData<Unit> by lazy { _closeDrawer }

    private val _onError by lazy { SingleLiveEvent<String>() }
    val onError: LiveData<String> by lazy { _onError }

    private val _priceAlertsCount by lazy { MutableLiveData<String>() }
    val priceAlertsCount: LiveData<String> by lazy { _priceAlertsCount }

    private val _enableNightMode by lazy { MutableLiveData<Boolean>() }
    val enableNightMode: LiveData<Boolean> by lazy { _enableNightMode }

    private val _recreate by lazy { SingleLiveEvent<Unit>() }
    val recreate by lazy { _recreate }

    private val actionsChannel = ConflatedBroadcastChannel<ViewAction>()

    internal val viewStateChannel = actionsChannel
        .asFlow()
        .flatMapLatest {
            flowOf(it).scan(HomeMviViewState()) { oldState, viewAction ->
                reduceInit(oldState)
            }
        }

    fun init() {
        viewModelScope.launch(dispatchers.Default) {
            actionsChannel.send(ViewAction.Init)
            kotlin.runCatching {
                _regionsLoading.postValue(true)

                getCurrentActiveRegion().also { activeRegion ->
                    _currentRegion.postValue(activeRegion.copy(regionCode = activeRegion.regionCode.toUpperCase()))
                }
            }.onFailure(onFailureHandler)
        }

        listenForNightModeChanges()
        listenForRegionChanges()
        listenForAlertsCountChanges()
    }

    fun toggleNightMode() = viewModelScope.launch {
        toggleNightModeUseCase()
    }

    fun closeDrawer() {
        _closeDrawer.postValue(Unit)
    }

    fun onNavigateTo(navigator: Navigator, uri: String, extras: Parcelable? = null) {
        navigator.navigate(uri, extras)
    }

    fun onRegionChangeClicked() =
        viewModelScope.launch(dispatchers.IO) {
            kotlin.runCatching {
                val activeRegion = getCurrentActiveRegion()
                _openRegionSelectionDialog.postValue(activeRegion)
            }.onFailure(onFailureHandler)
        }

    private fun listenForRegionChanges() =
        viewModelScope.launch(dispatchers.IO) {
            kotlin.runCatching {
                onActiveRegionChange.activeRegionChange().collect {
                    _currentRegion.postValue(it.copy(regionCode = it.regionCode.toUpperCase()))
                }
            }.onFailure(onFailureHandler)
        }

    private fun listenForAlertsCountChanges() = viewModelScope.launch(dispatchers.Default) {
        kotlin.runCatching {
            priceAlertsCountUseCase().collect {
                _priceAlertsCount.postValue(if (it == 0) "" else it.toString())
            }
        }.onFailure(onFailureHandler)
    }

    private fun listenForNightModeChanges() = viewModelScope.launch(dispatchers.Default) {
        kotlin.runCatching {
            onNightModeChangeUseCase.activeNightModeChange()
                .collect {
                    val newValue = it == NightMode.Enabled
                    if (newValue != _enableNightMode.value) {
                        _enableNightMode.postValue(it == NightMode.Enabled)
                        _recreate.postValue(Unit)
                    }
                }
        }.onFailure(onFailureHandler)
    }

    private suspend fun reduceInit(oldState: HomeMviViewState): HomeMviViewState {
        val drawerItems = mutableListOf<DrawerItem>()
        drawerItems.add(
            DrawerItem.TextWithIcon(
                icon = R.drawable.ic_deal,
                text = resourcesProvider.getString(R.string.title_on_going_deals),
                id = R.id.home_drawer_night_mode_switch
            )
        )
        drawerItems.add(
            DrawerItem.TextWithIcon(
                icon = R.drawable.ic_add_to_watch_list,
                text = resourcesProvider.getString(R.string.title_manage_your_watchlist),
                id = R.id.home_drawer_night_mode_switch + 1
            )
        )
        drawerItems.add(
            DrawerItem.SectionDivider(
                id = R.id.home_drawer_night_mode_switch + 2,
                text = resourcesProvider.getString(R.string.miscellaneous)
            )
        )


        val activeRegion = withContext(dispatchers.IO) { getCurrentActiveRegion() }

        drawerItems.add(
            DrawerItem.TextWithDescriptionAndIcon(
                icon = R.drawable.ic_region,
                id = R.id.home_drawer_night_mode_switch + 3,
                text = resourcesProvider.getString(R.string.change_region),
                description = activeRegion.country.displayName()
            )
        )
        val nightMode = onNightModeChangeUseCase.activeNightModeChange().first()
        drawerItems.add(
            DrawerItem.SwitchWithIcon(
                icon = R.drawable.ic_weather_night,
                id = R.id.home_drawer_night_mode_switch + 4,
                text = resourcesProvider.getString(R.string.enable_night_mode),
                isToggled = nightMode == NightMode.Enabled
            )
        )
        return oldState.copy(drawerItems = drawerItems)
    }

    private val onFailureHandler = { throwable: Throwable ->
        _onError.postValue(throwable.localizedMessage ?: throwable.message)
        Timber.e(throwable)
    }

    private companion object {
    }
}
