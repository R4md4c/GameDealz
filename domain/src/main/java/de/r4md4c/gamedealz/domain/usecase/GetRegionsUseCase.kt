package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.data.entity.RegionWithCountries

/**
 * Retrieves the current region.
 */
interface GetRegionsUseCase {

    /**
     * Retrieves available regions.
     *
     * @return a List of regions with their countries.
     */
    suspend fun regions(): List<RegionWithCountries>
}