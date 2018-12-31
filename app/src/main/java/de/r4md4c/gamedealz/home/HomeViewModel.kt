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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.r4md4c.gamedealz.common.GlobalExceptionHandler
import de.r4md4c.gamedealz.common.livedata.SingleLiveEvent
import de.r4md4c.gamedealz.common.navigator.Navigator
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.CollectionParameter
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.domain.usecase.ToggleStoresUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getCurrentActiveRegion: GetCurrentActiveRegionUseCase,
    private val onActiveRegionChange: OnCurrentActiveRegionReactiveUseCase,
    private val getStoresUseCase: GetStoresUseCase,
    private val toggleStoresUseCase: ToggleStoresUseCase
) : AbstractViewModel() {

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

    fun init() = uiScope.launch(GlobalExceptionHandler("Failure during init()")) {
        _regionsLoading.postValue(true)

        val activeRegion = getCurrentActiveRegion()

        _currentRegion.postValue(activeRegion.copy(regionCode = activeRegion.regionCode.toUpperCase()))

        listenForStoreChanges(activeRegion)

        uiScope.launch {
            onActiveRegionChange.activeRegionChange().consumeEach {
                _currentRegion.postValue(it.copy(regionCode = it.regionCode.toUpperCase()))
            }
        }
    }

    fun onStoreSelected(store: StoreModel) = uiScope.launch(GlobalExceptionHandler("Failed to select a store")) {
        toggleStoresUseCase(CollectionParameter(setOf(store)))
    }

    fun closeDrawer() {
        _closeDrawer.postValue(Unit)
    }

    fun onNavigateTo(navigator: Navigator, uri: String) {
        navigator.navigate(uri)
    }

    fun onRegionChangeClicked() {
        uiScope.launch(IO) {
            val activeRegion = getCurrentActiveRegion()
            _openRegionSelectionDialog.postValue(activeRegion)
        }
    }

    private fun listenForStoreChanges(activeRegion: ActiveRegion) = uiScope.launch(IO) {
        getStoresUseCase(TypeParameter(activeRegion)).consumeEach {
            _stores.postValue(it)
        }
    }

}