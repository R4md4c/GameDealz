package de.r4md4c.gamedealz.network.repository

import de.r4md4c.gamedealz.network.model.Store
import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import de.r4md4c.gamedealz.network.service.RegionCodes

class IsThereAnyDealRepository(private val service: IsThereAnyDealService) : RegionsRemoteRepository,
    StoresRemoteRepository {

    override suspend fun regions(): RegionCodes = service.regions().await().data

    override suspend fun stores(region: String, country: String?): List<Store> =
        service.stores(region, country).await().data

}
