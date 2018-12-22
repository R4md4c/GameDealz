package de.r4md4c.gamedealz.network.repository

import de.r4md4c.gamedealz.network.service.ShopPlains

interface PlainsRemoteRepository {

    /**
     * Retrieves IsThereAnyDeal plains list from a list of shops.
     */
    suspend fun plainsList(shops: Set<String>): ShopPlains

}
