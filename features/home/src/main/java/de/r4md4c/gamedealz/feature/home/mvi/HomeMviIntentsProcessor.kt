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

package de.r4md4c.gamedealz.feature.home.mvi

import de.r4md4c.gamedealz.common.mvi.IntentProcessor
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.intent
import de.r4md4c.gamedealz.common.mvi.uiSideEffect
import de.r4md4c.gamedealz.feature.home.R
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent.InitViewEvent
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent.NightModeToggleViewEvent
import de.r4md4c.gamedealz.feature.home.mvi.intent.InitIntent
import de.r4md4c.gamedealz.feature.home.mvi.intent.LogoutIntent
import de.r4md4c.gamedealz.feature.home.mvi.intent.NightModeToggleIntent
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.feature.home.state.HomeUiSideEffect
import javax.inject.Inject

internal class HomeMviIntentsProcessor @Inject constructor(
    private val store: ModelStore<HomeMviViewState>,
    private val initIntentFactory: InitIntent.Factory,
    private val nightModeToggleIntent: NightModeToggleIntent.Factory,
    private val logoutIntentFactory: LogoutIntent.Factory
) : IntentProcessor<HomeMviViewEvent> {

    override suspend fun process(viewEvent: HomeMviViewEvent) =
        store.process(createIntent(viewEvent))

    private fun createIntent(viewEvent: HomeMviViewEvent) = when (viewEvent) {
        is InitViewEvent -> initIntentFactory.create(viewEvent.scope, store)
        is NightModeToggleViewEvent -> nightModeToggleIntent.create(viewEvent.scope)
        is HomeMviViewEvent.LogoutViewEvent -> logoutIntentFactory.create(store)
        is HomeMviViewEvent.LoginViewEvent -> intent {
            copy(
                uiSideEffect = uiSideEffect {
                    HomeUiSideEffect.StartAuthenticationFlow
                })
        }
        is HomeMviViewEvent.NavigateToManageWatchlistScreen -> intent {
            copy(
                uiSideEffect = uiSideEffect {
                    HomeUiSideEffect.NavigateSideEffect(R.id.manageWatchlistFragment)
                }
            )
        }
        is HomeMviViewEvent.NavigateToOngoingDealsScreen -> intent {
            copy(
                uiSideEffect = uiSideEffect {
                    HomeUiSideEffect.NavigateSideEffect(R.id.dealsFragment, popToRoot = true)
                }
            )
        }
    }
}
