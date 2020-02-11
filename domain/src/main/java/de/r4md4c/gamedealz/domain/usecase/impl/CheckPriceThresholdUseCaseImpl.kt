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

import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.data.repository.RegionsLocalDataSource
import de.r4md4c.gamedealz.data.repository.WatchlistLocalDataSource
import de.r4md4c.gamedealz.data.repository.WatchlistStoresDataSource
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import de.r4md4c.gamedealz.domain.model.toCurrencyModel
import de.r4md4c.gamedealz.domain.model.toModel
import de.r4md4c.gamedealz.domain.model.toPriceModel
import de.r4md4c.gamedealz.domain.usecase.CheckPriceThresholdUseCase
import de.r4md4c.gamedealz.domain.usecase.impl.internal.PickMinimalWatcheesPricesHelper
import de.r4md4c.gamedealz.domain.usecase.impl.internal.PriceAlertsHelper
import de.r4md4c.gamedealz.domain.usecase.impl.internal.RetrievePricesGroupedByCountriesHelper
import de.r4md4c.gamedealz.network.model.PriceDTO
import timber.log.Timber
import javax.inject.Inject

internal class CheckPriceThresholdUseCaseImpl @Inject constructor(
    private val watchlistRepository: WatchlistLocalDataSource,
    private val watchlistStoresDataSource: WatchlistStoresDataSource,
    private val regionsRepository: RegionsLocalDataSource,
    private val retrievePricesGroupedByCountriesHelper: RetrievePricesGroupedByCountriesHelper,
    private val pickMinimalWatcheesPricesHelper: PickMinimalWatcheesPricesHelper,
    private val priceAlertsHelper: PriceAlertsHelper
) : CheckPriceThresholdUseCase {

    override suspend fun invoke(param: VoidParameter?): Set<WatcheeNotificationModel> {
        Timber.i("Starting checking for Prices.")
        val allWatcheesWithStores = watchlistStoresDataSource.allWatcheesWithStores().filter {
            // Filter out the Watchees that have already reached its target price.
            it.watchee.lastFetchedPrice > it.watchee.targetPrice
        }

        if (allWatcheesWithStores.isEmpty()) {
            Timber.d("Watch list is empty.")
            return emptySet()
        }

        val retrievedPrices: Map<String, List<PriceDTO>> =
            retrievePricesGroupedByCountriesHelper.prices(allWatcheesWithStores.map { it.watchee })

        val watcheesPriceModelMap = pickMinimalWatcheesPricesHelper.pick(retrievedPrices)

        if (watcheesPriceModelMap.isEmpty()) {
            return emptySet()
        }

        val watcheesNotificationModelsList = watcheesPriceModelMap.toList().mapNotNull { model ->
            // Get the most refreshed Snapshots.
            val refreshedSnapShot = watchlistRepository.findById(model.watcheeModel.id!!)
            refreshedSnapShot?.let { snapshot ->
                model.copy(watcheeModel = snapshot.toModel())
            }
        }

        Timber.d("Found new prices in these watchees $watcheesNotificationModelsList")

        priceAlertsHelper.storeNotificationModels(watcheesNotificationModelsList)

        return watcheesNotificationModelsList.toSet()
    }

    private suspend fun Map<PriceDTO, Watchee>.toList(): List<WatcheeNotificationModel> =
        this.mapNotNull {
            val watcheeModel = it.value.toModel()
            val priceModel = it.key.toPriceModel("") // We don't need the store color.
            val region = regionsRepository.findById(watcheeModel.regionCode) ?: return@mapNotNull null
            WatcheeNotificationModel(watcheeModel, priceModel, region.currency.toCurrencyModel())
        }
}
