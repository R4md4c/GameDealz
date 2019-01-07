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

package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.commonproviders.date.DateProvider
import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.data.repository.WatchlistRepository
import de.r4md4c.gamedealz.data.repository.WatchlistStoresRepository
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.WatcheeModel
import de.r4md4c.gamedealz.domain.model.toModel
import de.r4md4c.gamedealz.domain.usecase.CheckPriceThresholdUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.network.repository.PricesRemoteRepository
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.channels.firstOrNull
import timber.log.Timber
import java.util.concurrent.TimeUnit

internal class CheckPriceThresholdUseCaseImpl(
    private val watchlistRepository: WatchlistRepository,
    private val watchlistStoresRepository: WatchlistStoresRepository,
    private val pricesRemoteRepository: PricesRemoteRepository,
    private val currentActiveRegionUseCase: GetCurrentActiveRegionUseCase,
    private val dateProvider: DateProvider
) : CheckPriceThresholdUseCase {

    override suspend fun invoke(param: VoidParameter?): Set<WatcheeModel> {
        val activeRegion = currentActiveRegionUseCase()
        val allWatcheesWithStores = watchlistStoresRepository.allWatcheesWithStores().filter {
            // Filter out the Watchees that have already reached its target price.
            it.watchee.currentPrice > it.watchee.targetPrice
        }


        if (allWatcheesWithStores.isEmpty()) {
            Timber.d("Watch list is empty.")
            return emptySet()
        }

        val plainIdPricesMap = pricesRemoteRepository.retrievesPrices(
            plainIds = allWatcheesWithStores.asSequence().map { it.watchee.plainId }.toSet(),
            shops = allWatcheesWithStores.asSequence().flatMap { it.stores.asSequence() }.map { it.id }.toSet(),
            regionCode = activeRegion.regionCode,
            countryCode = activeRegion.country.code,
            added = allWatcheesWithStores.asSequence().map { it.watchee.lastCheckDate }.min()
        )

        val thresholdWatchees = mutableSetOf<Watchee>()
        plainIdPricesMap.forEach {
            val minPrice = it.value.firstOrNull() ?: return@forEach
            val watchee = watchlistRepository.findById(it.key).firstOrNull() ?: return@forEach

            if (minPrice.newPrice <= watchee.targetPrice) {
                thresholdWatchees.add(watchee)
                watchlistRepository.updateWatchee(
                    watchee.id, minPrice.newPrice,
                    TimeUnit.MILLISECONDS.toSeconds(dateProvider.timeInMillis())
                )
            }
        }

        if (thresholdWatchees.isEmpty()) {
            return emptySet()
        }

        // Get the most refreshed Snapshots.
        return watchlistRepository.all(thresholdWatchees.map { it.id }).first().map { it.toModel() }.toSet()
    }
}