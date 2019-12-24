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

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.r4md4c.gamedealz.common.mvi.Intent
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.intent
import de.r4md4c.gamedealz.common.mvi.uiSideEffect
import de.r4md4c.gamedealz.domain.usecase.LogoutUseCase
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.feature.home.state.HomeUiSideEffect
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class LogoutIntent @AssistedInject constructor(
    private val logoutUseCase: LogoutUseCase,
    @Assisted private val store: ModelStore<HomeMviViewState>
) : Intent<HomeMviViewState> {

    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.apply {
        GlobalScope.launch {
            // When calling this, the Flow will emit NotAuthorized AuthState in the InitIntent
            logoutUseCase()
            store.process(intent { copy(uiSideEffect = uiSideEffect { HomeUiSideEffect.NotifyUserHasLoggedOut }) })
        }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(store: ModelStore<HomeMviViewState>): Intent<HomeMviViewState>
    }
}
