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

import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.Resource
import de.r4md4c.gamedealz.domain.repository.GameDetailsRepository
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class GetPlainDetailsImpl @Inject constructor(
    private val gameDetailsRepository: GameDetailsRepository
) : GetPlainDetails {

    override suspend fun invoke(param: TypeParameter<String>?): Flow<Resource<PlainDetailsModel>> {
        return gameDetailsRepository.findDetails(param!!.value)
    } /*withContext(IO) {
        val plainId = requireNotNull(param).value

        val shopId = plainsRepository.findById(plainId)?.shopId

        val activeRegion = activeRegionUseCase()

        val deferredSteamResult: Deferred<AppDetails?>? = getSteamInfo(shopId)

        val steamResult = deferredSteamResult?.await()
        val shopPrices = asyncShopPricesWithHistoricalLows(plainId, activeRegion).await()

        PlainDetailsModel(
            currencyModel = activeRegion.currency,
            plainId = plainId,
            shopPrices = shopPrices,
            screenshots = steamResult?.screenshots?.map { ScreenshotModel(it.thumbnail, it.full) } ?: emptyList(),
            headerImage = steamResult?.headerImage,
            aboutGame = steamResult?.aboutGame,
            shortDescription = steamResult?.shortDescription,
            drmNotice = steamResult?.drmNotice)
    }

    private suspend fun retrieveSteamApp(shopId: String): AppDetails? {
        // If the id is an id of a package, then try to retrieve it's app details.
        return when {
            shopId.contains("sub") -> {
                val packageDetails = steamRemoteRepository.packageDetails(shopId.getIdFromSteamAppId())
                val appId = packageDetails?.apps?.firstOrNull()?.id
                appId?.let { steamRemoteRepository.appDetails(it) }
            }
            shopId.contains("app") -> steamRemoteRepository.appDetails(shopId.getIdFromSteamAppId())
            else -> null
        }
    }

    private suspend fun asyncShopPricesWithHistoricalLows(
        plainId: String,
        activeRegion: ActiveRegion
    ): Deferred<Map<ShopModel, PriceModelHistoricalLowModelPair>> = withContext(IO) {
        async {
            val currentPrices = pricesRemoteDataSource.retrievesPrices(
                plainIds = setOf(plainId),
                countryCode = activeRegion.country.code,
                regionCode = activeRegion.regionCode
            ).run { pricesToPriceModel(this[plainId]) }

            val shopPricesMap: Map<ShopModel, PriceModel> =
                currentPrices.groupBy { it.shop }.mapValues { it.value.first() }

            // Retrieve the historical lows by looping through the shops inside current prices and convert them to
            // Historical Models.
            val historicalLowPrices = currentPrices.mapNotNull {
                pricesRemoteDataSource.historicalLow(
                    setOf(plainId),
                    setOf(it.shop.id),
                    regionCode = activeRegion.regionCode,
                    countryCode = activeRegion.country.code
                )[plainId]
                    ?.run { historicalLowToModel(this) }
            }.groupBy { it.shop }.mapValues { it.value.first() }

            shopPricesMap.mapValues { PriceModelHistoricalLowModelPair(it.value, historicalLowPrices[it.key]) }
        }
    }

    private fun String.getIdFromSteamAppId() =
        substring(indexOf('/') + 1)

    private suspend fun pricesToPriceModel(prices: List<PriceDTO>?): List<PriceModel> =
        prices?.mapNotNull {
            val shop = storesRepository.findById(it.shop.id) ?: return@mapNotNull null
            it.toPriceModel(shop.color)
        } ?: emptyList()

    private suspend fun historicalLowToModel(historicalLow: HistoricalLowDTO): HistoricalLowModel? =
        historicalLow.run {
            if (this.shop != null) {
                val shop = storesRepository.findById(this.shop!!.id) ?: return@run null
                this.toModel(shop.color)
            } else {
                null
            }
        }

    private suspend fun getSteamInfo(shopId: String?): Deferred<AppDetails?>? = withContext(IO) {
        shopId?.let {
            async {
                runCatching { retrieveSteamApp(shopId) }
                    .onFailure {
                        Timber.w(it, "Failed to retrieve app details from steam.")
                    }.getOrNull()
            }
        }
    }*/
}
