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

package de.r4md4c.gamedealz.home

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import de.r4md4c.commonproviders.appcompat.NightMode
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.livedata.SingleLiveEvent
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.CollectionParameter
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.usecase.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val dispatchers: IDispatchers,
    private val getCurrentActiveRegion: GetCurrentActiveRegionUseCase,
    private val onActiveRegionChange: OnCurrentActiveRegionReactiveUseCase,
    private val getStoresUseCase: GetStoresUseCase,
    private val toggleStoresUseCase: ToggleStoresUseCase,
    private val priceAlertsCountUseCase: GetAlertsCountUseCase,
    private val toggleNightModeUseCase: ToggleNightModeUseCase,
    private val onNightModeChangeUseCase: OnNightModeChangeUseCase
) : AbstractViewModel(dispatchers) {

    private val _currentRegion by lazy { MutableLiveData<ActiveRegion>() }
    val currentRegion: LiveData<ActiveRegion> by lazy { _currentRegion }

    private val _regionsLoading by lazy { MutableLiveData<Boolean>() }
    val regionsLoading: LiveData<Boolean> by lazy { _regionsLoading }

    private val _stores by lazy { MutableLiveData<List<StoreModel>>() }
    val stores: LiveData<List<StoreModel>> by lazy { _stores }

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

    fun init() {
        viewModelScope.launch(dispatchers.Default) {
            kotlin.runCatching {
                _regionsLoading.postValue(true)

                getCurrentActiveRegion().also { activeRegion ->
                    _currentRegion.postValue(activeRegion.copy(regionCode = activeRegion.regionCode.toUpperCase()))
                }
            }.onSuccess { listenForStoreChanges(it) }.onFailure(onFailureHandler)
        }

        listenForNightModeChanges()
        listenForRegionChanges()
        listenForAlertsCountChanges()
    }


    fun onStoreSelected(store: StoreModel) = viewModelScope.launch {
        kotlin.runCatching {
            toggleStoresUseCase(CollectionParameter(setOf(store)))
        }.onFailure(onFailureHandler)
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

    private fun listenForStoreChanges(activeRegion: ActiveRegion) = viewModelScope.launch(dispatchers.IO) {
        kotlin.runCatching {
            getStoresUseCase(TypeParameter(activeRegion)).collect {
                _stores.postValue(it)
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

    private val onFailureHandler = { throwable: Throwable ->
        _onError.postValue(throwable.localizedMessage ?: throwable.message)
        Timber.e(throwable)
    }
}