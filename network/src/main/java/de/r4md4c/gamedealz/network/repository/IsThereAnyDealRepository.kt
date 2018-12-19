package de.r4md4c.gamedealz.network.repository

import de.r4md4c.gamedealz.network.model.Deal
import de.r4md4c.gamedealz.network.model.PageResult
import de.r4md4c.gamedealz.network.model.Store
import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import de.r4md4c.gamedealz.network.service.RegionCodes

internal class IsThereAnyDealRepository(private val service: IsThereAnyDealService) : RegionsRemoteRepository,
    StoresRemoteRepository, DealsRemoteRepository {

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
            shops = shops.fold("") { acc, value -> "$acc$value," }).await().run {
            PageResult(this.data.count ?: 0, this.data.list)
        }
}
