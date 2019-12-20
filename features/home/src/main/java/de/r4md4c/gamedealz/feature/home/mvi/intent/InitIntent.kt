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
import de.r4md4c.commonproviders.appcompat.NightMode
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.mvi.Intent
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.UiSideEffect
import de.r4md4c.gamedealz.common.mvi.intent
import de.r4md4c.gamedealz.common.mvi.uiSideEffect
import de.r4md4c.gamedealz.domain.model.UserInfo
import de.r4md4c.gamedealz.domain.usecase.GetAlertsCountUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetUserUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.domain.usecase.OnNightModeChangeUseCase
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.feature.home.state.HomeUiSideEffect
import de.r4md4c.gamedealz.feature.home.state.HomeUserStatus
import de.r4md4c.gamedealz.feature.home.state.PriceAlertCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class InitIntent @AssistedInject constructor(
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val onRegionChangeUseCase: OnCurrentActiveRegionReactiveUseCase,
    private val nightModeChangeUseCase: OnNightModeChangeUseCase,
    private val priceAlertsCountUseCase: GetAlertsCountUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val dispatchers: IDispatchers,
    @Assisted private val store: ModelStore<HomeMviViewState>,
    @Assisted private val scope: CoroutineScope
) : Intent<HomeMviViewState> {

    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.apply {
        loadRegions()

        observeRegionChange()
        observeNightModeChanges()
        observePriceAlertCount()
        observeUser()
    }

    private fun observeUser() = scope.launch {
        getUserUseCase().mapNotNull {
            when (it) {
                is UserInfo.LoggingUserFailed -> {
                    store.process(intent {
                        copy(
                            uiSideEffect = UiSideEffect(
                                HomeUiSideEffect.ShowAuthenticationError(it.reason)
                            )
                        )
                    })
                    null
                }
                is UserInfo.UserLoggedOut -> HomeUserStatus.UserNotLoggedIn
                is UserInfo.LoggedInUser -> HomeUserStatus.UserLoggedIn(it.username)
            }
        }.distinctUntilChanged().collect {
            store.process(intent {
                copy(
                    homeUserStatus = it,
                    uiSideEffect = if (it is HomeUserStatus.UserLoggedIn) uiSideEffect {
                        HomeUiSideEffect.NotifyUserHasLoggedIn(
                            it.username
                        )
                    } else null
                )
            })
        }
    }

    private fun loadRegions() = scope.launch {
        store.process(intent { copy(isLoadingRegions = true) })

        kotlin.runCatching {
            val activeRegion = withContext(dispatchers.IO) { activeRegionUseCase() }

            store.process(intent {
                copy(activeRegion = activeRegion)
            })
        }

        store.process(intent { copy(isLoadingRegions = false) })
    }

    private fun observeRegionChange() = scope.launch(dispatchers.IO) {
        // Drop the first one since it is being loaded from loadRegions
        onRegionChangeUseCase.activeRegionChange().drop(1).collect { region ->
            withContext(dispatchers.Main) {
                store.process(intent { copy(activeRegion = region) })
            }
        }
    }

    private fun observeNightModeChanges() = scope.launch {
            nightModeChangeUseCase.activeNightModeChange().collect { nightMode ->
                store.process(intent {
                    copy(nightModeEnabled = nightMode == NightMode.Enabled)
                })
            }
    }

    private fun observePriceAlertCount() = scope.launch(dispatchers.IO) {
        priceAlertsCountUseCase().collect { count ->
            store.process(intent { copy(priceAlertsCount = count.priceAlertFromCount()) })
        }
    }

    private fun Int.priceAlertFromCount(): PriceAlertCount =
        if (this == 0) PriceAlertCount.NotSet else PriceAlertCount.Set(this)

    @AssistedInject.Factory
    interface Factory {
        fun create(
            scope: CoroutineScope,
            store: ModelStore<HomeMviViewState>
        ): Intent<HomeMviViewState>
    }
}
