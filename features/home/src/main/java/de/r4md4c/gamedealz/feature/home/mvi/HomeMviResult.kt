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

import androidx.annotation.IdRes
import de.r4md4c.gamedealz.common.mvi.ReducibleMviResult
import de.r4md4c.gamedealz.common.mvi.uiSideEffect
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.feature.home.state.HomeUiSideEffect
import de.r4md4c.gamedealz.feature.home.state.HomeUserStatus
import de.r4md4c.gamedealz.feature.home.state.PriceAlertCount

sealed class HomeMviResult : ReducibleMviResult<HomeMviViewState>

internal object LogoutResult : HomeMviResult() {
    override fun reduce(oldState: HomeMviViewState): HomeMviViewState =
        oldState.copy(uiSideEffect = uiSideEffect { HomeUiSideEffect.NotifyUserHasLoggedOut })
}

internal object NavigateToLoginResult : HomeMviResult() {
    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.run {
        copy(uiSideEffect = uiSideEffect { HomeUiSideEffect.StartAuthenticationFlow })
    }
}

internal data class NavigationResult(
    @IdRes val navigationIdentifier: Int,
    val popToRoot: Boolean = false
) : HomeMviResult() {
    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.run {
        copy(
            uiSideEffect = uiSideEffect {
                HomeUiSideEffect.NavigateSideEffect(
                    navigationIdentifier,
                    popToRoot
                )
            }
        )
    }
}

internal data class ToggleNightModeResult(val shouldEnable: Boolean) : HomeMviResult() {
    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.run {
        copy(
            nightModeEnabled = shouldEnable
        )
    }
}

internal data class PriceAlertCountResult(val currentCount: PriceAlertCount) : HomeMviResult() {
    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.run {
        copy(priceAlertsCount = currentCount)
    }
}

internal data class RegionsLoadingResult(val isLoading: Boolean) : HomeMviResult() {
    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.run {
        copy(isLoadingRegions = isLoading)
    }
}

internal data class ActiveRegionResult(val region: ActiveRegion) : HomeMviResult() {
    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.run {
        copy(activeRegion = region)
    }
}

internal data class AnonymousUserResult(val shouldNotify: Boolean) :
    HomeMviResult() {
    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.run {
        val sideEffect = if (shouldNotify) {
            uiSideEffect { HomeUiSideEffect.NotifyUserHasLoggedIn(null) }
        } else {
            null
        }
        copy(homeUserStatus = HomeUserStatus.LoggedIn.UnknownUser, uiSideEffect = sideEffect)
    }
}

internal data class KnownUserResult(val shouldNotify: Boolean, val userName: String) :
    HomeMviResult() {
    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.run {
        val sideEffect = if (shouldNotify) {
            uiSideEffect { HomeUiSideEffect.NotifyUserHasLoggedIn(userName) }
        } else {
            null
        }
        copy(
            homeUserStatus = HomeUserStatus.LoggedIn.KnownUser(userName),
            uiSideEffect = sideEffect
        )
    }
}

internal object UserLoggedOutResult : HomeMviResult() {
    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.run {
        copy(homeUserStatus = HomeUserStatus.LoggedOut)
    }
}

internal data class LoginFailedResult(val reason: String) : HomeMviResult() {
    override fun reduce(oldState: HomeMviViewState): HomeMviViewState = oldState.run {
        copy(uiSideEffect = uiSideEffect {
            HomeUiSideEffect.ShowAuthenticationError(reason)
        })
    }
}
