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

package de.r4md4c.gamedealz.feature.home.mvi.processor

import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.mvi.IntentProcessor
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.feature.home.mvi.ActiveRegionResult
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviResult
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent
import de.r4md4c.gamedealz.feature.home.mvi.RegionsLoadingResult
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

internal class RegionsInitIntentProcessor @Inject constructor(
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val onRegionChangeUseCase: OnCurrentActiveRegionReactiveUseCase,
    private val getStoresUseCase: GetStoresUseCase,
    private val dispatchers: IDispatchers
) : IntentProcessor<HomeMviViewEvent, HomeMviViewState> {

    override fun process(viewEvent: Flow<HomeMviViewEvent>): Flow<HomeMviResult> =
        viewEvent.filterIsInstance<HomeMviViewEvent.InitViewEvent>()
            .flatMapMerge(concurrency = 2) {
                merge(retrieveActiveRegions(), regionChangeFlow())
            }

    private fun retrieveActiveRegions(): Flow<HomeMviResult> = flow {
        emit(RegionsLoadingResult(isLoading = true))

        kotlin.runCatching {
            withContext(dispatchers.IO) { activeRegionUseCase() }
        }.onSuccess {
            emit(ActiveRegionResult(it))
        }.onFailure { Timber.e(it, "Failure while retrieving action region") }

        emit(RegionsLoadingResult(isLoading = false))
    }

    private fun regionChangeFlow(): Flow<HomeMviResult> =
        onRegionChangeUseCase.activeRegionChange().onEach {
            getStoresUseCase(TypeParameter(it))
                .catch { e -> Timber.e(e, "Failed while retrieving the stores.") }
                .first()
        }.map { ActiveRegionResult(it) }
}
