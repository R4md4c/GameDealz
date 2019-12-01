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

package de.r4md4c.gamedealz.feature.deals

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Config
import androidx.paging.DataSource
import androidx.paging.toLiveData
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.feature.deals.model.DealRenderModel
import de.r4md4c.gamedealz.domain.usecase.GetSelectedStoresUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject

class DealsViewModel @Inject constructor(
    private val dispatchers: IDispatchers,
    private val factory: DataSource.Factory<Int, DealRenderModel>,
    private val selectedStoresUseCase: GetSelectedStoresUseCase,
    private val uiStateMachineDelegate: StateMachineDelegate
) : ViewModel() {

    val deals by lazy {
        factory.toLiveData(
            Config(
                pageSize = BuildConfig.DEFAULT_PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSizeHint = BuildConfig.DEFAULT_PAGE_SIZE * RATIO
            )
        )
    }

    val sideEffect: MutableLiveData<SideEffect> by lazy {
        MutableLiveData<SideEffect>()
    }

    fun init() {
        viewModelScope.launch(dispatchers.IO) {
            selectedStoresUseCase().debounce(DEBOUNCE_TIMEOUT_MILLIS).drop(1).collect {
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

    companion object {
        private const val RATIO = 2
        private const val DEBOUNCE_TIMEOUT_MILLIS = 500L
    }
}
