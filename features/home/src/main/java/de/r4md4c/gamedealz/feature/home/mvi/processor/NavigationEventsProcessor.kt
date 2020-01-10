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

import de.r4md4c.gamedealz.common.mvi.Intent
import de.r4md4c.gamedealz.common.mvi.IntentProcessor
import de.r4md4c.gamedealz.common.mvi.intent
import de.r4md4c.gamedealz.common.mvi.uiSideEffect
import de.r4md4c.gamedealz.feature.home.R
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.feature.home.state.HomeUiSideEffect.NavigateSideEffect
import de.r4md4c.gamedealz.feature.home.state.HomeUiSideEffect.StartAuthenticationFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

internal class NavigationEventsProcessor @Inject constructor() :
    IntentProcessor<HomeMviViewEvent, HomeMviViewState> {

    override fun process(viewEvent: Flow<HomeMviViewEvent>): Flow<Intent<HomeMviViewState>> =
        merge(
            viewEvent.filterIsInstance<HomeMviViewEvent.LoginViewEvent>(),
            viewEvent.filterIsInstance<HomeMviViewEvent.NavigateToManageWatchlistScreen>(),
            viewEvent.filterIsInstance<HomeMviViewEvent.NavigateToOngoingDealsScreen>()
        ).map {
            when (it) {
                is HomeMviViewEvent.LoginViewEvent -> intent<HomeMviViewState> {
                    copy(
                        uiSideEffect = uiSideEffect {
                            StartAuthenticationFlow
                        }
                    )
                }
                is HomeMviViewEvent.NavigateToManageWatchlistScreen -> intent {
                    copy(
                        uiSideEffect = uiSideEffect {
                            NavigateSideEffect(R.id.manageWatchlistFragment)
                        }
                    )
                }
                is HomeMviViewEvent.NavigateToOngoingDealsScreen -> intent {
                    copy(
                        uiSideEffect = uiSideEffect {
                            NavigateSideEffect(R.id.dealsFragment, popToRoot = true)
                        }
                    )
                }
                else -> throw IllegalArgumentException("Unsupported event type: $it")
            }
        }
}
