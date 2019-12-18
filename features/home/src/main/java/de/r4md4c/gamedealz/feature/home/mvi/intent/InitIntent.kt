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

package de.r4md4c.gamedealz.feature.home.mvi.intent

import com.squareup.inject.assisted.AssistedInject
import de.r4md4c.commonproviders.appcompat.NightMode
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.mvi.Intent
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.intent
import de.r4md4c.gamedealz.domain.usecase.GetAlertsCountUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.domain.usecase.OnNightModeChangeUseCase
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.feature.home.state.PriceAlertCount
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class InitIntent @AssistedInject constructor(
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val onRegionChangeUseCase: OnCurrentActiveRegionReactiveUseCase,
    private val nightModeChangeUseCase: OnNightModeChangeUseCase,
    private val priceAlertsCountUseCase: GetAlertsCountUseCase,
    private val homeMviStore: ModelStore<HomeMviViewState>,
    private val dispatchers: IDispatchers
) : Intent<HomeMviViewState> {

    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.apply {
        loadRegions()

        observeRegionChange()
        observeNightModeChanges()
        observePriceAlertCount()
    }

    private fun loadRegions() = homeMviStore.launch {
        homeMviStore.process(intent { copy(isLoadingRegions = true) })

        kotlin.runCatching {
            val activeRegion = withContext(dispatchers.IO) { activeRegionUseCase() }

            homeMviStore.process(intent {
                copy(activeRegion = activeRegion)
            })
        }

        homeMviStore.process(intent { copy(isLoadingRegions = false) })
    }

    private fun observeRegionChange() = homeMviStore.launch(dispatchers.IO) {
        // Drop the first one since it is being loaded from loadRegions
        onRegionChangeUseCase.activeRegionChange().drop(1).collect { region ->
            withContext(dispatchers.Main) {
                homeMviStore.process(intent { copy(activeRegion = region) })
            }
        }
    }

    private fun observeNightModeChanges() {
        homeMviStore.launch {
            nightModeChangeUseCase.activeNightModeChange().collect { nightMode ->
                homeMviStore.process(intent {
                    copy(nightModeEnabled = nightMode == NightMode.Enabled)
                })
            }
        }
    }

    private fun observePriceAlertCount() = homeMviStore.launch(dispatchers.IO) {
        priceAlertsCountUseCase().collect { count ->
            homeMviStore.process(intent { copy(priceAlertsCount = count.priceAlertFromCount()) })
        }
    }

    private fun Int.priceAlertFromCount(): PriceAlertCount =
        if (this == 0) PriceAlertCount.NotSet else PriceAlertCount.Set(this)

    @AssistedInject.Factory
    interface Factory {
        fun create(): Intent<HomeMviViewState>
    }
}
