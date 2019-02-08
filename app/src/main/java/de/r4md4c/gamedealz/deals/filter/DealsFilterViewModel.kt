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

package de.r4md4c.gamedealz.deals.filter

import androidx.collection.ArraySet
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.launchWithCatching
import de.r4md4c.gamedealz.common.livedata.SingleLiveEvent
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.deals.item.FilterItem
import de.r4md4c.gamedealz.domain.CollectionParameter
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.ToggleStoresUseCase
import kotlinx.coroutines.channels.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class DealsFilterViewModel(
    private val dispatchers: IDispatchers,
    private val getCurrentActiveRegion: GetCurrentActiveRegionUseCase,
    private val getStoresUseCase: GetStoresUseCase,
    private val toggleStoresUseCase: ToggleStoresUseCase
) : AbstractViewModel(dispatchers) {

    private val toggleSet: MutableSet<StoreModel> by lazy {
        ArraySet<StoreModel>()
    }

    /**
     * Holds all the stores that are not filtered.
     */
    private var _originalStores: List<FilterItem> = emptyList()

    private val _stores by lazy { MutableLiveData<List<FilterItem>>() }
    val stores: LiveData<List<FilterItem>> by lazy { _stores }

    private val _dismiss by lazy { SingleLiveEvent<Unit>() }
    val dismiss: LiveData<Unit> by lazy { _dismiss }

    fun loadStores() = uiScope.launchWithCatching(dispatchers.Main, {
        val stores = withContext(dispatchers.IO) {
            val activeRegion = getCurrentActiveRegion()
            getStoresUseCase(TypeParameter(activeRegion)).firstOrNull()
        } ?: return@launchWithCatching


        val filterItems = withContext(dispatchers.Default) {
            stores.map { FilterItem(it).withSetSelected(it.selected) }
        }
        _originalStores = filterItems
        _stores.postValue(filterItems)
    }) {
        Timber.e(it, "Failed to load stores in DealsFilterViewModel.")
    }

    fun onSearchTextChanged(newText: String) {
        uiScope.launch(dispatchers.Default) {
            val filteredStore = _originalStores.filter { it.storeModel.name.startsWith(newText, ignoreCase = true) }
                .map { it.withSetSelected(toggleSet.contains(it.storeModel)) }
            _stores.postValue(filteredStore)
        }
    }

    fun onSelection(item: FilterItem, selected: Boolean) {
        if (item.storeModel.selected != selected) {
            toggleSet.add(item.storeModel)
        } else {
            toggleSet.remove(item.storeModel)
        }
    }

    fun submit() = uiScope.launchWithCatching(dispatchers.IO, {
        toggleStoresUseCase(CollectionParameter(toggleSet))
        _dismiss.postValue(Unit)
    }) {
        Timber.e("Failed to toggle stores.")
    }
}