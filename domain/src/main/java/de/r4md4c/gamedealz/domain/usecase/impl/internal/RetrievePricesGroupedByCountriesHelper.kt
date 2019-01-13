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

package de.r4md4c.gamedealz.domain.usecase.impl.internal

import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.network.model.Price
import de.r4md4c.gamedealz.network.repository.PricesRemoteRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

/**
 * A Helper method used in [de.r4md4c.gamedealz.domain.usecase.impl.CheckPriceThresholdUseCaseImpl],
 * to retrieve the prices from a group of watchees with their stores.
 */
internal class RetrievePricesGroupedByCountriesHelper(private val pricesRemoteRepository: PricesRemoteRepository) {

    /**
     * Gets the prices from the watchees that are grouped by countries.
     * With each different country we request prices for it.
     *
     * @param watchees the watchees that you want them to be queried.
     * @return A map of plain Ids and the list of their prices from the server.
     */
    suspend fun prices(watchees: Iterable<Watchee>): Map<String, List<Price>> {
        val countryGroupedWatchees = watchees.groupBy { it.regionCode to it.countryCode }

        val resultMap = mutableMapOf<String, List<Price>>()
        val retrievedPricesMapList: List<Deferred<Map<String, List<Price>>>> = countryGroupedWatchees.map { entry ->
            GlobalScope.async {
                pricesRemoteRepository.retrievesPrices(
                    plainIds = entry.value.asSequence().map { it.plainId }.toSet(),
                    regionCode = entry.key.first,
                    countryCode = entry.key.second,
                    added = entry.value.asSequence()
                        .map { if (it.lastCheckDate == 0L) it.dateAdded else it.lastCheckDate }
                        .max()
                )
            }
        }

        retrievedPricesMapList.awaitAll().forEach { resultMap.putAll(it) }
        return resultMap
    }
}