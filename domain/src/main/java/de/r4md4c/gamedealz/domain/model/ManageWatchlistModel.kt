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
 * Used to describe list items in the ManageWatchlist screen.
 *
 * @param watcheeModel contains the information about the watched game.
 * @param hasNotification Indicates if this model has something to notify the user about. (Like highlighting the ListItem)
 * @param currencyModel the currency that this model was watched for.
 */
data class ManageWatchlistModel(
    val watcheeModel: WatcheeModel,
    val hasNotification: Boolean = false,
    val currencyModel: CurrencyModel
)
