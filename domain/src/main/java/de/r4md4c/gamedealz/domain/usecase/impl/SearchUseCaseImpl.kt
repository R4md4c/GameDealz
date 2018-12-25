package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.SearchResultModel
import de.r4md4c.gamedealz.domain.model.toModel
import de.r4md4c.gamedealz.domain.model.toPriceModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetSelectedStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.SearchUseCase
import de.r4md4c.gamedealz.network.repository.PricesRemoteRepository
import de.r4md4c.gamedealz.network.service.SearchService
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.withContext

internal class SearchUseCaseImpl(
    private val searchService: SearchService,
    private val pricesRemoteRepository: PricesRemoteRepository,
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val selectedStoresUseCase: GetSelectedStoresUseCase
) : SearchUseCase {

    override suspend fun invoke(param: TypeParameter<String>?): List<SearchResultModel> = withContext(IO) {
        val searchTerm = requireNotNull(param).value

        val activeRegion = activeRegionUseCase()
        val selectedStores = selectedStoresUseCase()

        // Execute the search query.
        val searchResults = searchService.search(searchTerm)

        val searchResultsPlainId = searchResults.mapTo(mutableSetOf()) { it.plain.value }
        val storeIds = selectedStores.first().mapTo(mutableSetOf()) { it.id }

        val prices = pricesRemoteRepository.retrievesPrices(
            searchResultsPlainId,
            storeIds, activeRegion.regionCode, activeRegion.country.code
        )

        val historicalLow = pricesRemoteRepository.historicalLow(
            searchResultsPlainId,
            storeIds,
            activeRegion.regionCode,
            activeRegion.country.code
        )

        searchResults
            .filter { prices[it.plain.value]?.isNotEmpty() ?: false }
            .map {
                SearchResultModel(title = it.title, gameId = it.plain.value,
                    prices = prices[it.plain.value]?.map { p -> p.toPriceModel() } ?: emptyList(),
                    historicalLow = historicalLow[it.plain.value]?.toModel())
            }
    }
}