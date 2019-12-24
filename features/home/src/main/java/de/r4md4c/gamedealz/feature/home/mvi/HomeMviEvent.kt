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

import de.r4md4c.gamedealz.common.mvi.MviViewEvent
import kotlinx.coroutines.CoroutineScope

internal sealed class HomeMviViewEvent : MviViewEvent {
    data class InitViewEvent(val scope: CoroutineScope) : HomeMviViewEvent()
    data class NightModeToggleViewEvent(val scope: CoroutineScope) : HomeMviViewEvent()
    object NavigateToManageWatchlistScreen : HomeMviViewEvent() {
        override fun toString(): String = "NavigateToManageWatchlist"
    }

    object NavigateToOngoingDealsScreen : HomeMviViewEvent() {
        override fun toString(): String = "NavigateToOngoingDealsScreen"
    }

    object LogoutViewEvent : HomeMviViewEvent() {
        override fun toString(): String = "LogoutViewEvent"
    }

    object LoginViewEvent : HomeMviViewEvent() {
        override fun toString(): String = "LoginViewEvent"
    }
}
