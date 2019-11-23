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

import de.r4md4c.commonproviders.coroutines.GameDealzDispatchers.IO
import de.r4md4c.gamedealz.data.repository.StoresRepository
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.*
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetImageUrlUseCase
import de.r4md4c.gamedealz.domain.usecase.SearchUseCase
import de.r4md4c.gamedealz.network.model.HistoricalLow
import de.r4md4c.gamedealz.network.model.Price
import de.r4md4c.gamedealz.network.repository.PricesRemoteRepository
import de.r4md4c.gamedealz.network.service.SearchService
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

internal class SearchUseCaseImpl(
    private val searchService: SearchService,
    private val pricesRemoteRepository: PricesRemoteRepository,
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val storesRepository: StoresRepository,
    private val imageUrlUseCase: GetImageUrlUseCase
) : SearchUseCase {

    override suspend fun invoke(param: TypeParameter<String>?): List<SearchResultModel> = withContext(IO) {
        val searchTerm = requireNotNull(param).value

        val activeRegion = activeRegionUseCase()

        // Execute the search query.
        val searchResults = searchService.search(searchTerm)

        check(isActive) { "Search was cancelled" }

        if (searchResults.isEmpty()) return@withContext emptyList<SearchResultModel>()

        val searchResultsPlainId = searchResults.mapTo(mutableSetOf()) { it.plain.value }

        val prices = pricesRemoteRepository.retrievesPrices(
            searchResultsPlainId,
            emptySet(), activeRegion.regionCode, activeRegion.country.code
        )

        check(isActive) { "Search was cancelled" }

        val historicalLow = pricesRemoteRepository.historicalLow(
            searchResultsPlainId,
            emptySet(),
            activeRegion.regionCode,
            activeRegion.country.code
        )

        searchResults
            .filter { prices[it.plain.value]?.isNotEmpty() ?: false }
            .mapNotNull {
                SearchResultModel(title = it.title, gameId = it.plain.value,
                    prices = prices[it.plain.value]?.pricesWithStoreColor() ?: emptyList(),
                    historicalLow = historicalLow[it.plain.value]?.toHistoricalModelWithColor(),
                    imageUrl = imageUrlUseCase(TypeParameter(it.plain.value)),
                    currencyModel = activeRegion.currency
                )
            }
    }

    private suspend fun List<Price>.pricesWithStoreColor(): List<PriceModel> =
        mapNotNull {
            val storeColor = storesRepository.findById(it.shop.id)?.color ?: return@mapNotNull null
            it.toPriceModel(storeColor)
        }

    private suspend fun HistoricalLow.toHistoricalModelWithColor(): HistoricalLowModel? {
        val shopId = shop?.id ?: return null
        val color = storesRepository.findById(shopId)?.color ?: return null
        return this.toModel(color)
    }
}
