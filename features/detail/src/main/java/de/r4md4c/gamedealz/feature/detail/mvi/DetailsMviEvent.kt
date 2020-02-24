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

package de.r4md4c.gamedealz.feature.detail.mvi

import de.r4md4c.gamedealz.common.mvi.MviInitEvent
import de.r4md4c.gamedealz.common.mvi.UIEvent
import de.r4md4c.gamedealz.feature.detail.PriceDetails

sealed class DetailsMviEvent : MviInitEvent {
    data class InitEvent(val plainId: String) : DetailsMviEvent()
    data class PriceFilterChangeEvent(val sortOrder: SortOrder) : DetailsMviEvent()
    object RetryClickEvent : DetailsMviEvent()
    object WatchlistFabClickEvent : DetailsMviEvent()
    object RemoveFromWatchlistYes : DetailsMviEvent()
}

sealed class DetailsUIEvent : UIEvent {
    object AskUserToRemoveFromWatchlist : DetailsUIEvent()
    data class NavigateToAddToWatchlistScreen(val priceDetails: PriceDetails) : DetailsUIEvent()
    data class NotifyRemoveFromWatchlistSuccessfully(val gameTitle: String) : DetailsUIEvent()
}
