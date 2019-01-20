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

import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.data.repository.PlainsRepository
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.PlainResultModel
import de.r4md4c.gamedealz.domain.model.toModel
import de.r4md4c.gamedealz.domain.model.toPriceModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetHighlightsUseCase
import de.r4md4c.gamedealz.network.repository.PricesRemoteRepository
import de.r4md4c.gamedealz.network.repository.SteamRemoteRepository
import de.r4md4c.gamedealz.network.service.HighlightsService
import kotlinx.coroutines.withContext

internal class GetHighlightsUseCaseImpl(
    private val highlightsService: HighlightsService,
    private val plainsRepository: PlainsRepository,
    private val pricesRemoteRepository: PricesRemoteRepository,
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val steamRemoteRepository: SteamRemoteRepository,
    private val dispatchers: IDispatchers
) : GetHighlightsUseCase {

    override suspend fun invoke(param: VoidParameter?): List<PlainResultModel> = withContext(dispatchers.IO) {
        val activeRegion = activeRegionUseCase()

        val highlightsPlains = highlightsService.highlights(
            regionCode = activeRegion.regionCode,
            countryCode = activeRegion.country.code
        ).map { it.value }.toSet()

        val appDetails = highlightsPlains.mapNotNull { plainsRepository.findById(it) }
            .associateBy(keySelector = { it.id })
            { steamRemoteRepository.appDetails(it.shopId.getIdFromSteamAppId()) }

        val prices = pricesRemoteRepository.retrievesPrices(
            highlightsPlains,
            regionCode = activeRegion.regionCode, countryCode = activeRegion.country.code
        )

        val historicalLows = pricesRemoteRepository.historicalLow(
            highlightsPlains, regionCode = activeRegion.regionCode,
            countryCode = activeRegion.country.code
        )


        highlightsPlains.mapNotNull { plainId ->
            val appDetail = appDetails[plainId] ?: return@mapNotNull null
            val plainPrices = prices[plainId]?.map { it.toPriceModel("") } ?: return@mapNotNull null
            val historicalLow = historicalLows[plainId] ?: return@mapNotNull null
            PlainResultModel(
                title = appDetail.name,
                currencyModel = activeRegion.currency,
                gameId = plainId,
                prices = plainPrices,
                historicalLow = historicalLow.toModel(""),
                imageUrl = appDetail.headerImage
            )
        }
    }

    private fun String.getIdFromSteamAppId() =
        substring(indexOf('/') + 1)

}