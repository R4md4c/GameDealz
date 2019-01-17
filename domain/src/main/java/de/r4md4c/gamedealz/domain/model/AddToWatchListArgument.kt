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

package de.r4md4c.gamedealz.domain.model

/**
 * A Value type that describe a request to add a game to the watchlist.
 *
 * @param plainId the plain id of the game.
 * @param title the title of the game.
 * @param currentPrice the currentPrice of the game at the time that it was added.
 * @param currentStoreName the current store that the current price is from.
 * @param targetPrice the desired price that you want to set the alert for.
 * @param stores the stores that will be observed for the price change
 */
data class AddToWatchListArgument(
    val plainId: String,
    val title: String,
    val currentPrice: Float,
    val currentStoreName: String,
    val targetPrice: Float,
    val stores: List<StoreModel>
)

