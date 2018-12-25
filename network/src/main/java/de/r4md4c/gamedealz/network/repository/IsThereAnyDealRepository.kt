package de.r4md4c.gamedealz.network.repository

import de.r4md4c.gamedealz.network.model.*
import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import de.r4md4c.gamedealz.network.service.RegionCodes
import de.r4md4c.gamedealz.network.service.ShopPlains

internal class IsThereAnyDealRepository(private val service: IsThereAnyDealService) : RegionsRemoteRepository,
    StoresRemoteRepository, DealsRemoteRepository, PlainsRemoteRepository, PricesRemoteRepository {

    override suspend fun regions(): RegionCodes = service.regions().await().data

    override suspend fun stores(region: String, country: String?): List<Store> =
        service.stores(region, country).await().data

    override suspend fun deals(
        offset: Int,
        limit: Int,
        region: String,
        country: String,
        shops: Set<String>
    ): PageResult<Deal> =
        service.deals(
            offset = offset,
            limit = limit,
            region = region,
            country = country,
            shops = shops.toCommaSeparated()
        ).await().run {
            PageResult(this.data.count ?: 0, this.data.list)
        }

    override suspend fun plainsList(shops: Set<String>): ShopPlains =
        service.allPlains(shops = shops.fold("") { acc, value -> "$acc$value," }).await().data

    override suspend fun retrievesPrices(
        plainIds: Set<String>,
        shops: Set<String>,
        regionCode: String?,
        countryCode: String?,
        added: Int?
    ): Map<String, List<Price>> =
        service.prices(
            plains = plainIds.toCommaSeparated(),
            shops = shops.toCommaSeparated(),
            region = regionCode,
            country = countryCode,
            added = added
        ).await().data.mapValues { it.value.list }

    override suspend fun historicalLow(
        plainIds: Set<String>,
        shops: Set<String>,
        regionCode: String?,
        countryCode: String?
    ): Map<String, HistoricalLow> =
        service.historicalLow(
            plains = plainIds.toCommaSeparated(),
            shops = shops.toCommaSeparated(),
            region = regionCode,
            country = countryCode
        ).await().data

    private fun Set<String>.toCommaSeparated() =
        foldIndexed("") { index, acc, value -> "$acc$value${if (index == size - 1) "" else ","}" }
}
