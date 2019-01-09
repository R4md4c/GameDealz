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
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.data.repository.StoresRepository
import de.r4md4c.gamedealz.data.repository.WatchlistRepository
import de.r4md4c.gamedealz.data.repository.WatchlistStoresRepository
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.AddToWatchListArgument
import de.r4md4c.gamedealz.domain.model.WatcheeModel
import de.r4md4c.gamedealz.domain.model.toRepositoryModel
import de.r4md4c.gamedealz.domain.usecase.AddToWatchListUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

internal class AddToWatchListUseCaseImpl(
    private val dispatchers: IDispatchers,
    private val watchlistStoresRepository: WatchlistStoresRepository,
    private val watchlistRepository: WatchlistRepository,
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val storesRepository: StoresRepository,
    private val dateProvider: DateProvider
) : AddToWatchListUseCase {

    override suspend fun invoke(param: TypeParameter<AddToWatchListArgument>?) {
        val parameter = requireNotNull(param).value
        val stores = parameter.stores

        withContext(dispatchers.IO) {
            val activeRegion = activeRegionUseCase()
            val watcheeModel = with(parameter) {
                WatcheeModel(
                    plainId = plainId,
                    title = title,
                    currentPrice = currentPrice,
                    targetPrice = targetPrice,
                    regionCode = activeRegion.regionCode,
                    countryCode = activeRegion.country.code,
                    currencyCode = activeRegion.currency.currencyCode
                )
            }
            val savedRepositoryStores = storesRepository.all(stores.map { it.id }).first()
            val repositoryModel = watcheeModel
                .copy(dateAdded = TimeUnit.MILLISECONDS.toMillis(dateProvider.timeInMillis()))
                .toRepositoryModel()

            runCatching {
                watchlistStoresRepository.saveWatcheeWithStores(repositoryModel, savedRepositoryStores)
            }.onFailure {
                watchlistRepository.removeById(parameter.plainId)
            }.getOrThrow()
        }
    }
}