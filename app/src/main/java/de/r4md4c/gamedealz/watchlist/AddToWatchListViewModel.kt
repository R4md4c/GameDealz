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
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import timber.log.Timber

class AddToWatchListViewModel(
    private val dispatchers: IDispatchers,
    private val getCurrentActiveRegionUseCase: GetCurrentActiveRegionUseCase,
    private val getStoresUseCase: GetStoresUseCase
) : AbstractViewModel(dispatchers) {

    private val _availableStores by lazy { MutableLiveData<List<StoreModel>>() }

    fun loadStores(): LiveData<List<StoreModel>> {
        uiScope.launch(dispatchers.IO) {
            kotlin.runCatching {
                val activeRegion = getCurrentActiveRegionUseCase()
                val stores = getStoresUseCase.invoke(TypeParameter(activeRegion)).first()
                _availableStores.postValue(stores)
            }.onFailure { Timber.e(it, "Failed to load the stores") }
        }

        return _availableStores
    }
}