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

package de.r4md4c.gamedealz.feature.home.state

import androidx.annotation.IdRes
import de.r4md4c.gamedealz.common.mvi.MviState
import de.r4md4c.gamedealz.common.mvi.UiSideEffect
import de.r4md4c.gamedealz.domain.model.ActiveRegion

internal data class HomeMviViewState(
    val activeRegion: ActiveRegion? = null,
    val homeUserStatus: HomeUserStatus = HomeUserStatus.LoggedOut,
    val isLoadingRegions: Boolean = false,
    val nightModeEnabled: Boolean = false,
    val priceAlertsCount: PriceAlertCount = PriceAlertCount.NotSet,
    val uiSideEffect: UiSideEffect<HomeUiSideEffect>? = null
) : MviState

sealed class PriceAlertCount {
    object NotSet : PriceAlertCount() {
        override fun toString(): String = "NotSet"
    }

    data class Set(val count: Int) : PriceAlertCount()
}

internal sealed class HomeUiSideEffect {
    data class ShowAuthenticationError(val message: String?) : HomeUiSideEffect()

    data class NotifyUserHasLoggedIn(val username: String?) : HomeUiSideEffect()

    class NavigateSideEffect(
        @IdRes val navigationIdentifier: Int,
        val popToRoot: Boolean = false
    ) : HomeUiSideEffect()

    object NotifyUserHasLoggedOut : HomeUiSideEffect() {
        override fun toString(): String = "NotifyUserHasLoggedOut"
    }

    object StartAuthenticationFlow : HomeUiSideEffect() {
        override fun toString(): String = "StartAuthenticationFlow"
    }
}
