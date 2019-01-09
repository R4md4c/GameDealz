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
import de.r4md4c.gamedealz.data.repository.RegionsRepository
import de.r4md4c.gamedealz.data.repository.WatchlistRepository
import de.r4md4c.gamedealz.data.repository.WatchlistStoresRepository
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import de.r4md4c.gamedealz.domain.model.toCurrencyModel
import de.r4md4c.gamedealz.domain.model.toModel
import de.r4md4c.gamedealz.domain.model.toPriceModel
import de.r4md4c.gamedealz.domain.usecase.CheckPriceThresholdUseCase
import de.r4md4c.gamedealz.network.model.Price
import de.r4md4c.gamedealz.network.repository.PricesRemoteRepository
import kotlinx.coroutines.channels.firstOrNull
import timber.log.Timber
import java.util.concurrent.TimeUnit

internal class CheckPriceThresholdUseCaseImpl(
    private val watchlistRepository: WatchlistRepository,
    private val watchlistStoresRepository: WatchlistStoresRepository,
    private val pricesRemoteRepository: PricesRemoteRepository,
    private val dateProvider: DateProvider,
    private val regionsRepository: RegionsRepository
) : CheckPriceThresholdUseCase {

    override suspend fun invoke(param: VoidParameter?): Set<WatcheeNotificationModel> {
        Timber.i("Starting checking for Prices.")
        val allWatcheesWithStores = watchlistStoresRepository.allWatcheesWithStores().filter {
            // Filter out the Watchees that have already reached its target price.
            it.watchee.currentPrice > it.watchee.targetPrice
        }


        if (allWatcheesWithStores.isEmpty()) {
            Timber.d("Watch list is empty.")
            return emptySet()
        }

        // To handle case users having watch list from two different countries and regions.
        val countryGroupedWatchees = allWatcheesWithStores.groupBy { it.watchee.regionCode to it.watchee.countryCode }

        val plainIdPricesMap = mutableMapOf<String, List<Price>>()
        countryGroupedWatchees.map { entry ->
            pricesRemoteRepository.retrievesPrices(
                plainIds = entry.value.asSequence().map { it.watchee.plainId }.toSet(),
                shops = entry.value.asSequence().flatMap { it.stores.asSequence() }.map { it.id }.toSet(),
                regionCode = entry.key.first,
                countryCode = entry.key.second,
                added = entry.value.asSequence()
                    .map { if (it.watchee.lastCheckDate == 0L) it.watchee.dateAdded else it.watchee.lastCheckDate }
                    .max()
            )
        }.forEach {
            plainIdPricesMap.putAll(it)
        }

        val watcheesPriceModelMap = mutableMapOf<Price, Watchee>()
        plainIdPricesMap.forEach {
            val minPrice = it.value.firstOrNull() ?: return@forEach
            val watchee = watchlistRepository.findById(it.key).firstOrNull() ?: return@forEach
            watchlistRepository.updateWatchee(
                watchee.id, minPrice.newPrice,
                TimeUnit.MILLISECONDS.toSeconds(dateProvider.timeInMillis())
            )

            if (minPrice.newPrice <= watchee.targetPrice) {
                watcheesPriceModelMap[minPrice] = watchee
            }
        }


        if (watcheesPriceModelMap.isEmpty()) {
            return emptySet()
        }

        val watcheesNotificationModelsList = watcheesPriceModelMap.toList().mapNotNull {
            // Get the most refreshed Snapshots.
            val refreshedSnapShot = watchlistRepository.findById(it.watcheeModel.id!!) ?: return@mapNotNull null
            it.copy(watcheeModel = refreshedSnapShot.toModel())
        }

        Timber.d("Found new prices in these watchees $watcheesNotificationModelsList")

        return watcheesNotificationModelsList.toSet()
    }

    private suspend fun Map<Price, Watchee>.toList(): List<WatcheeNotificationModel> =
        this.mapNotNull {
            val watcheeModel = it.value.toModel()
            val priceModel = it.key.toPriceModel("") // We don't need the store color.
            val region = regionsRepository.findById(watcheeModel.regionCode) ?: return@mapNotNull null
            WatcheeNotificationModel(watcheeModel, priceModel, region.currency.toCurrencyModel())
        }
}