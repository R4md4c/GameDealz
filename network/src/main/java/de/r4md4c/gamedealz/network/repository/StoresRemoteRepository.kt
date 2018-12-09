package de.r4md4c.gamedealz.network.repository

import de.r4md4c.gamedealz.network.model.Store

interface StoresRemoteRepository {

    /**
     * Retrieves the stores from the network.
     * You should always specify region and country to narrow down the search.
     *
     * @param region the region.
     * @param country the country could be null as well.
     * @return a List of stores, under this region and country.
     */
    suspend fun stores(region: String, country: String? = null): List<Store>
}