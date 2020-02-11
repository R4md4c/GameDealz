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

import de.r4md4c.commonproviders.date.DateProvider
import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.data.repository.WatchlistLocalDataSource
import de.r4md4c.gamedealz.data.repository.WatchlistStoresDataSource
import de.r4md4c.gamedealz.network.model.PriceDTO
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * A Helper class to pick the lowest prices of the watchees that have reacheed the threshold.
 * This respects also the stores that are available for these watchees.
 */
internal class PickMinimalWatcheesPricesHelper @Inject constructor(
    private val watchlistRepository: WatchlistLocalDataSource,
    private val watchlistStoresDataSource: WatchlistStoresDataSource,
    private val dateProvider: DateProvider
) {

    /**
     * Pick the articles that has the reached the target price.
     *
     * @param prices A Map between the plain Ids and list of prices of that was retrieved from the server.
     * @return A map between the minimum price as key and the [Watchee] model that has reached the target price.
     */
    suspend fun pick(prices: Map<String, List<PriceDTO>>): Map<PriceDTO, Watchee> {
        return prices.mapNotNull {
            val watchee = watchlistRepository.findById(it.key).first() ?: return@mapNotNull null
            val watcheesStoresIds =
                watchlistStoresDataSource.findWatcheeWithStores(watchee)?.stores?.map { s -> s.id }
                    ?: return@mapNotNull null
            val minPrice =
                it.value.firstOrNull { price -> watcheesStoresIds.contains(price.shop.id) } ?: return@mapNotNull null

            watchlistRepository.updateWatchee(
                watchee.id, minPrice.newPrice, minPrice.shop.name,
                TimeUnit.MILLISECONDS.toSeconds(dateProvider.timeInMillis())
            )

            if (minPrice.newPrice <= watchee.targetPrice) {
                minPrice to watchee
            } else null
        }.toMap()
    }
}
