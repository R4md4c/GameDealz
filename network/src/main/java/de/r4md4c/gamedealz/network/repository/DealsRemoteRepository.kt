package de.r4md4c.gamedealz.network.repository

import de.r4md4c.gamedealz.network.model.Deal
import de.r4md4c.gamedealz.network.model.PageResult

interface DealsRemoteRepository {

    /**
     * Retrieve the current deals from the network.
     *
     * @param offset the offset to start the deals from.
     * @param limit the page ize
     * @param region the region code
     * @param country the country code
     * @param shops the shops ids to search for.
     */
    suspend fun deals(offset: Int, limit: Int, region: String, country: String, shops: Set<String>): PageResult<Deal>

}