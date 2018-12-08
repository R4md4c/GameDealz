package de.r4md4c.gamedealz.network.repository

import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import de.r4md4c.gamedealz.network.service.RegionCodes

class IsThereAnyDealRepository(private val service: IsThereAnyDealService) : RegionsRepository {

    override suspend fun regions(): RegionCodes = service.regions().await().data

}
