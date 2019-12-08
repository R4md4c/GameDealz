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

package de.r4md4c.gamedealz.feature.watchlist

import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.model.ManageWatchlistModel
import de.r4md4c.gamedealz.domain.model.WatcheeModel

object Fixtures {

    fun manageWatchlistModel(
        watcheeModel: WatcheeModel = watcheeModel(),
        hasNotification: Boolean = true,
        currencyModel: CurrencyModel = currencyModel()
    ) =
        ManageWatchlistModel(
            watcheeModel = watcheeModel,
            hasNotification = hasNotification,
            currencyModel = currencyModel
        )

    fun watcheeModel(
        id: Long = 1,
        plainId: String = "plainId",
        title: String = "title",
        dateAdded: Long = 0,
        lastCheckDate: Long = 0,
        lastFetchedPrice: Float = 0f,
        targetPrice: Float = 1f,
        lastFetchedStoreName: String = "lastFetchedStoreName",
        regionCode: String = "regionCode",
        countryCode: String = "countryCode",
        currencyCode: String = "code"
    ) =
        WatcheeModel(
            id, plainId, title, dateAdded, lastCheckDate,
            lastFetchedPrice, targetPrice, lastFetchedStoreName, regionCode, countryCode, currencyCode
        )

    fun currencyModel(currencyCode: String = "currencyCode", sign: String = "$") = CurrencyModel(currencyCode, sign)
}
