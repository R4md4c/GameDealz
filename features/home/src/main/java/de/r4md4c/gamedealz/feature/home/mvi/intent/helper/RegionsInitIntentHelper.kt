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

package de.r4md4c.gamedealz.feature.home.mvi.intent.helper

import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.intent
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

internal class RegionsInitIntentHelper @Inject constructor(
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val onRegionChangeUseCase: OnCurrentActiveRegionReactiveUseCase,
    private val dispatchers: IDispatchers
) {

    fun CoroutineScope.observeRegions(store: ModelStore<HomeMviViewState>) {
        launch {
            retrieveActiveRegions(store)
        }
        launch {
            regionChangeFlow().collect {
                store.process(intent { copy(activeRegion = it) })
            }
        }
    }

    private suspend fun retrieveActiveRegions(store: ModelStore<HomeMviViewState>) {
        store.process(intent { copy(isLoadingRegions = true) })

        kotlin.runCatching {
            withContext(dispatchers.IO) { activeRegionUseCase() }
        }.onSuccess {
            store.process(intent {
                copy(activeRegion = it)
            })
        }.onFailure { Timber.e(it, "Failure while retrieving action region") }

        store.process(intent { copy(isLoadingRegions = false) })
    }

    private fun regionChangeFlow() = onRegionChangeUseCase.activeRegionChange()
}
