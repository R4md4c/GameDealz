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

package de.r4md4c.gamedealz.network.repository

import de.r4md4c.gamedealz.network.model.HistoricalLow
import de.r4md4c.gamedealz.network.model.Price

interface PricesRemoteRepository {

    /**
     * Retrieve prices for a list of plain ids.
     *
     * @param plainIds the game plain ids that is going to be retrieved.
     * @param shops the shops that these plain ids will be searched for.
     * @param regionCode Region code to get more accurate results.
     * @param countryCode The country code, along with region code to get more accurate results.
     * @param added the unix timestamp to get prices after that period.
     *
     * @return A map between the requested plain ids and the list of prices that was retrieved.
     */
    suspend fun retrievesPrices(
        plainIds: Set<String>,
        shops: Set<String> = emptySet(),
        regionCode: String? = null,
        countryCode: String? = null,
        added: Long? = null
    ): Map<String, List<Price>>

    /**
     * Retrieve the historical low for a list of plain ids.
     *
     * @param plainIds the game plain ids that is going to be retrieved.
     * @param shops the shops that these plain ids will be searched for.
     * @param regionCode Region code to get more accurate results.
     * @param countryCode The country code, along with region code to get more accurate results.
     *
     * @return A map between the requested plain ids and the historical low of that game.
     */
    suspend fun historicalLow(
        plainIds: Set<String>,
        shops: Set<String> = emptySet(),
        regionCode: String? = null,
        countryCode: String? = null
    ): Map<String, HistoricalLow>
}
