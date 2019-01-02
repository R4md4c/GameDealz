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

package de.r4md4c.gamedealz.deals

import androidx.lifecycle.MutableLiveData
import androidx.paging.Config
import androidx.paging.DataSource
import androidx.paging.toLiveData
import de.r4md4c.commonproviders.coroutines.IDispatchers
import de.r4md4c.gamedealz.BuildConfig
import de.r4md4c.gamedealz.common.debounce
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.usecase.GetSelectedStoresUseCase
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.drop
import kotlinx.coroutines.launch

class DealsViewModel(
    private val dispatchers: IDispatchers,
    private val factory: DataSource.Factory<Int, DealModel>,
    private val selectedStoresUseCase: GetSelectedStoresUseCase,
    private val uiStateMachineDelegate: StateMachineDelegate
) : AbstractViewModel(dispatchers) {

    val deals by lazy {
        factory.toLiveData(
            Config(
                pageSize = BuildConfig.DEFAULT_PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSizeHint = BuildConfig.DEFAULT_PAGE_SIZE * 2
            )
        )
    }

    val sideEffect: MutableLiveData<SideEffect> by lazy {
        MutableLiveData<SideEffect>()
    }

    fun init() {
        uiScope.launch(dispatchers.IO) {
            selectedStoresUseCase().debounce(uiScope, 500).drop(1).consumeEach {
                deals.value?.dataSource?.invalidate()
            }
        }

        uiStateMachineDelegate.onTransition {
            sideEffect.postValue(it)
        }
        deals.value?.dataSource?.invalidate()
    }

    fun onRefresh() {
        deals.value?.dataSource?.invalidate()
    }
}