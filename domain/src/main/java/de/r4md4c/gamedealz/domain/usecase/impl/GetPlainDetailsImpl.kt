package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.gamedealz.data.repository.PlainsRepository
import de.r4md4c.gamedealz.data.repository.StoresRepository
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.*
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import de.r4md4c.gamedealz.network.model.HistoricalLow
import de.r4md4c.gamedealz.network.model.Price
import de.r4md4c.gamedealz.network.model.steam.AppDetails
import de.r4md4c.gamedealz.network.repository.PricesRemoteRepository
import de.r4md4c.gamedealz.network.repository.SteamRemoteRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class GetPlainDetailsImpl(
    private val steamRemoteRepository: SteamRemoteRepository,
    private val plainsRepository: PlainsRepository,
    private val pricesRemoteRepository: PricesRemoteRepository,
    private val storesRepository: StoresRepository,
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase
) : GetPlainDetails {

    override suspend fun invoke(param: TypeParameter<String>?): PlainDetailsModel = withContext(IO) {
        val plainId = requireNotNull(param).value

        val shopId = plainsRepository.findById(plainId)?.shopId

        val activeRegion = activeRegionUseCase()

        val deferredSteamResult: Deferred<AppDetails?>? = if (shopId == null) {
            null
        } else {
            async {
                runCatching { retrieveSteamApp(shopId) }
                    .onFailure {
                        Timber.w(it, "Failed to retrieve app details from steam.")
                    }.getOrNull()
            }
        }

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
    ): Deferred<Map<ShopModel, Pair<PriceModel, HistoricalLowModel?>>> = withContext(IO) {
        async {
            val currentPrices = pricesRemoteRepository.retrievesPrices(
                plainIds = setOf(plainId),
                countryCode = activeRegion.country.code,
                regionCode = activeRegion.regionCode
            ).run { pricesToPriceModel(this[plainId]) }

            val shopPricesMap: Map<ShopModel, PriceModel> =
                currentPrices.groupBy { it.shop }.mapValues { it.value.first() }

            // Retrieve the historical lows by looping through the shops inside current prices and convert them to
            // Historical Models.
            val historicalLowPrices = currentPrices.mapNotNull {
                pricesRemoteRepository.historicalLow(
                    setOf(plainId),
                    setOf(it.shop.id),
                    regionCode = activeRegion.regionCode,
                    countryCode = activeRegion.country.code
                )[plainId]
                    ?.run { historicalLowToModel(this) }
            }.groupBy { it.shop }.mapValues { it.value.first() }

            shopPricesMap.mapValues { it.value to historicalLowPrices[it.key] }

        }
    }

    private fun String.getIdFromSteamAppId() =
        substring(indexOf('/') + 1)

    private suspend fun pricesToPriceModel(prices: List<Price>?): List<PriceModel> =
        prices?.mapNotNull {
            val shop = storesRepository.findById(it.shop.id) ?: return@mapNotNull null
            it.toPriceModel(shop.color)
        } ?: emptyList()

    private suspend fun historicalLowToModel(historicalLow: HistoricalLow): HistoricalLowModel? =
        historicalLow.run {
            if (this.shop != null) {
                val shop = storesRepository.findById(this.shop!!.id) ?: return@run null
                this.toModel(shop.color)
            } else {
                null
            }
        }


}