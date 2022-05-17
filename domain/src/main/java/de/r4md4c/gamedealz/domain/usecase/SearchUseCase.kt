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

package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.commonproviders.coroutines.GameDealzDispatchers.IO
import de.r4md4c.gamedealz.common.runSuspendCatching
import de.r4md4c.gamedealz.data.repository.StoresLocalDataSource
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.HistoricalLowModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.SearchResultModel
import de.r4md4c.gamedealz.domain.model.toModel
import de.r4md4c.gamedealz.domain.model.toPriceModel
import de.r4md4c.gamedealz.network.model.HistoricalLowDTO
import de.r4md4c.gamedealz.network.model.PriceDTO
import de.r4md4c.gamedealz.network.repository.PricesRemoteDataSource
import de.r4md4c.gamedealz.network.service.SearchService
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchUseCase @Inject internal constructor(
    private val searchService: SearchService,
    private val pricesRemoteDataSource: PricesRemoteDataSource,
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val storesRepository: StoresLocalDataSource,
    private val imageUrlUseCase: GetImageUrlUseCase
) {

    suspend fun invoke(searchTerm: String): Result<List<SearchResultModel>> =
        runSuspendCatching {
            withContext(IO) {
                val activeRegion = activeRegionUseCase()

                // Execute the search query.
                val searchResults = searchService.search(searchTerm)

                if (searchResults.isEmpty()) return@withContext emptyList<SearchResultModel>()

                val searchResultsPlainId = searchResults.mapTo(mutableSetOf()) { it.plain.value }

                val prices = pricesRemoteDataSource.retrievesPrices(
                    searchResultsPlainId,
                    emptySet(), activeRegion.regionCode, activeRegion.country.code
                )

                val historicalLow = pricesRemoteDataSource.historicalLow(
                    searchResultsPlainId,
                    emptySet(),
                    activeRegion.regionCode,
                    activeRegion.country.code
                )

                searchResults
                    .filter { prices[it.plain.value]?.isNotEmpty() ?: false }
                    .mapNotNull {
                        SearchResultModel(
                            title = it.title, gameId = it.plain.value,
                            prices = prices[it.plain.value]?.pricesWithStoreColor() ?: emptyList(),
                            historicalLow = historicalLow[it.plain.value]?.toHistoricalModelWithColor(),
                            imageUrl = imageUrlUseCase(TypeParameter(it.plain.value)),
                            currencyModel = activeRegion.currency
                        )
                    }
            }
        }

    private suspend fun List<PriceDTO>.pricesWithStoreColor(): List<PriceModel> =
        mapNotNull {
            val storeColor = storesRepository.findById(it.shop.id)?.color ?: return@mapNotNull null
            it.toPriceModel(storeColor)
        }

    private suspend fun HistoricalLowDTO.toHistoricalModelWithColor(): HistoricalLowModel? {
        val shopId = shop?.id ?: return null
        val color = storesRepository.findById(shopId)?.color ?: return null
        return this.toModel(color)
    }
}
