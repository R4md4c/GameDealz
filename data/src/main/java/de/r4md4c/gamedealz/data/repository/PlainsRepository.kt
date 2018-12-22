package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.entity.Plain

interface PlainsRepository : Repository<Plain, String> {

    /**
     * Counts the number of items in that repository.
     *
     * @return the number of items.
     */
    suspend fun count(): Int
}