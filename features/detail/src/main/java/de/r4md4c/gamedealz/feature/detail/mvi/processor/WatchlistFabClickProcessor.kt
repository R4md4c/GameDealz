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

package de.r4md4c.gamedealz.feature.detail.mvi.processor

import de.r4md4c.gamedealz.common.mvi.IntentProcessor
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.MviResult
import de.r4md4c.gamedealz.common.mvi.UIEventsDispatcher
import de.r4md4c.gamedealz.common.mvi.ignoreResult
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.usecase.RemoveFromWatchlistUseCase
import de.r4md4c.gamedealz.feature.detail.DetailsFragmentArgs
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsMviEvent
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsUIEvent
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsViewState
import de.r4md4c.gamedealz.feature.detail.mvi.Section
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

internal class WatchlistFabClickProcessor @Inject constructor(
    private val detailsFragmentArgs: DetailsFragmentArgs,
    private val stateStore: ModelStore<DetailsViewState>,
    private val uiDispatcher: UIEventsDispatcher<DetailsUIEvent>,
    private val removeFromWatchlist: RemoveFromWatchlistUseCase
) : IntentProcessor<DetailsMviEvent, DetailsViewState> {

    override fun process(
        viewEvent: Flow<DetailsMviEvent>
    ): Flow<MviResult<DetailsViewState>> =
        listOf(viewEvent.handleFabClickEvent(), viewEvent.handleRemoveFromWatchlist()).merge()

    private fun Flow<DetailsMviEvent>.handleRemoveFromWatchlist() =
        filterIsInstance<DetailsMviEvent.RemoveFromWatchlistYes>()
            .onEach {
                val isRemoved = removeFromWatchlist(TypeParameter(detailsFragmentArgs.plainId))
                if (isRemoved) {
                    uiDispatcher.dispatchEvent(
                        DetailsUIEvent.NotifyRemoveFromWatchlistSuccessfully(
                            detailsFragmentArgs.title
                        )
                    )
                }
            }.ignoreResult<DetailsViewState>()

    private fun Flow<DetailsMviEvent>.handleFabClickEvent() =
        filterIsInstance<DetailsMviEvent.WatchlistFabClickEvent>()
            .onEach {
                if (stateStore.currentState.isWatched == false) {
                    val currentState = stateStore.currentState
                    val pricesSection =
                        currentState.sections.filterIsInstance<Section.PriceSection>()
                            .firstOrNull() ?: return@onEach
                    val priceDetails = pricesSection.priceDetails.firstOrNull() ?: return@onEach

                    uiDispatcher.dispatchEvent(
                        DetailsUIEvent.NavigateToAddToWatchlistScreen(
                            priceDetails
                        )
                    )
                } else if (stateStore.currentState.isWatched == true) {
                    uiDispatcher.dispatchEvent(DetailsUIEvent.AskUserToRemoveFromWatchlist)
                }
            }.ignoreResult<DetailsViewState>()
}
