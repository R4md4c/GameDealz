package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.gamedealz.data.repository.PlainsRepository
import de.r4md4c.gamedealz.data.repository.StoresRepository
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.toPriceModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import de.r4md4c.gamedealz.network.model.Price
import de.r4md4c.gamedealz.network.model.steam.AppDetails
import de.r4md4c.gamedealz.network.repository.PricesRemoteRepository
import de.r4md4c.gamedealz.network.repository.SteamRemoteRepository
import kotlinx.coroutines.Dispatchers.IO
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

        val steamResult: AppDetails? = if (shopId == null) {
            null
        } else {
            runCatching { retrieveSteamApp(shopId) }
                .onFailure {
                    Timber.w(it, "Failed to retrieve app details from steam.")
                }.getOrNull()
        }

        val isThereAnyDealPrices = pricesRemoteRepository.retrievesPrices(
            setOf(plainId),
            countryCode = activeRegion.country.code, regionCode = activeRegion.regionCode
        )


        PlainDetailsModel(plainId = plainId,
            prices = pricesToPriceModel(isThereAnyDealPrices[plainId]),
            screenshots = steamResult?.screenshots?.map { it.thumbnail } ?: emptyList(),
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

    private fun String.getIdFromSteamAppId() =
        substring(indexOf('/') + 1)

    private suspend fun pricesToPriceModel(prices: List<Price>?): List<PriceModel> =
        prices?.mapNotNull {
            val shop = storesRepository.findById(it.shop.id) ?: return@mapNotNull null
            it.toPriceModel(shop.color)
        } ?: emptyList()
}