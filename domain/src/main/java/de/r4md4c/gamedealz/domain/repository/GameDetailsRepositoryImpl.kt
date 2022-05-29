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

package de.r4md4c.gamedealz.domain.repository

import com.dropbox.android.external.store4.MemoryPolicy
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.repository.PlainsLocalDataSource
import de.r4md4c.gamedealz.data.repository.StoresLocalDataSource
import de.r4md4c.gamedealz.domain.mapper.Mapper
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.HistoricalLowModel
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.PriceModelHistoricalLowModelPair
import de.r4md4c.gamedealz.domain.model.ShopModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.network.model.HistoricalLowDTO
import de.r4md4c.gamedealz.network.model.PriceDTO
import de.r4md4c.gamedealz.network.model.steam.AppDetailsDTO
import de.r4md4c.gamedealz.network.repository.PricesRemoteDataSource
import de.r4md4c.gamedealz.network.repository.SteamRemoteDataSource
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GameDetailsRepositoryImpl @Inject constructor(
    private val gamePricesRemoteDataSource: PricesRemoteDataSource,
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val steamRemoteDataSource: SteamRemoteDataSource,
    private val plainsDataSource: PlainsLocalDataSource,
    private val storesLocalDataSource: StoresLocalDataSource,
    private val storeEntityMapper: Mapper<Store, ShopModel>,
    private val historicalLowDTOMapper: Mapper<HistoricalLowDTO, HistoricalLowModel>,
    private val pricesDTOMapper: Mapper<PriceDTO, PriceModel>,
    private val appDetailsMapper: Mapper<AppDetailsDTO, PlainDetailsModel.GameArtworkDetails>
) : GameDetailsRepository {

    private val store by lazy {
        StoreBuilder.fromNonFlow<String, PlainDetailsModel> { plainId ->
            val activeRegion = activeRegionUseCase.invoke()
            val prices = retrievePrices(plainId, activeRegion)
            val historicalLows =
                prices.map { historicalLowAsync(plainId, it.key, activeRegion) }.toMap()
            val shopId = plainsDataSource.findById(plainId)?.shopId
            val steamAppDetails = shopId?.let { retrieveSteamApp(shopId) }
            PlainDetailsModel(
                currencyModel = activeRegion.currency,
                plainId = plainId,
                shopPrices = prices.entries.associate {
                    it.key to PriceModelHistoricalLowModelPair(
                        prices[it.key]!!,
                        historicalLows[it.key]
                    )
                },
                gameArtworkDetails = steamAppDetails?.let(appDetailsMapper::map),
                drmNotice = null
            )
        }.cachePolicy(
            MemoryPolicy.MemoryPolicyBuilder()
                .setExpireAfterTimeUnit(TimeUnit.MINUTES)
                .setExpireAfterWrite(EXPIRE_DURATION_MINUTES) // Expire after 30 Minutes
                .build()
        ).build()
    }

    override fun findDetails(
        plainId: String,
        fresh: Boolean
    ): Flow<StoreResponse<PlainDetailsModel>> = store.stream(StoreRequest.cached(plainId, fresh))

    private suspend fun historicalLowAsync(
        plainId: String,
        shopId: ShopModel,
        activeRegion: ActiveRegion
    ): Pair<ShopModel, HistoricalLowModel> =
        gamePricesRemoteDataSource.historicalLow(
            setOf(plainId),
            shops = setOf(shopId.id),
            regionCode = activeRegion.regionCode,
            countryCode = activeRegion.country.code
        ).asSequence()
            .map { it.value }
            .filter { it.shop != null }
            .associateBy { it.shop!! }
            .mapKeys { storesLocalDataSource.findById(it.key.id)!! }
            .mapKeys { storeEntityMapper.map(it.key) }
            .asSequence()
            .associateBy(
                keySelector = { it.key },
                valueTransform = { historicalLowDTOMapper.map(it.value) }
            )
            .toList()
            .first()

    private suspend fun retrievePrices(
        plainId: String,
        activeRegion: ActiveRegion
    ): Map<ShopModel, PriceModel> {
        return gamePricesRemoteDataSource.retrievesPrices(
            setOf(plainId),
            regionCode = activeRegion.regionCode,
            countryCode = activeRegion.country.code
        ).asSequence()
            .flatMap { it.value.asSequence() }
            .associateBy { it.shop }
            .mapKeys { storesLocalDataSource.findById(it.key.id) }
            .filterNot { it.key == null }
            .mapKeys { storeEntityMapper.map(it.key!!) }
            .mapValues { pricesDTOMapper.map(it.value) }
    }

    private suspend fun retrieveSteamApp(shopId: String): AppDetailsDTO? {
        // If the id is an id of a package, then try to retrieve it's app details.
        return when {
            shopId.contains("sub") -> {
                val packageDetails =
                    steamRemoteDataSource.packageDetails(shopId.getIdFromSteamAppId())
                val appId = packageDetails?.apps?.firstOrNull()?.id
                appId?.let { steamRemoteDataSource.appDetails(it) }
            }
            shopId.contains("app") -> steamRemoteDataSource.appDetails(shopId.getIdFromSteamAppId())
            else -> null
        }
    }

    private fun String.getIdFromSteamAppId() =
        substring(indexOf('/') + 1)

    private companion object {
        private const val EXPIRE_DURATION_MINUTES = 30L
    }
}
