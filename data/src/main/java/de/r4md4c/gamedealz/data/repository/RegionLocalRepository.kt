package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.dao.RegionWithCountriesDao
import de.r4md4c.gamedealz.data.entity.RegionWithCountries

internal class RegionLocalRepository(private val regionWithCountriesDao: RegionWithCountriesDao) : RegionsRepository {

    override suspend fun all(): List<RegionWithCountries> = regionWithCountriesDao.allRegions()

    override suspend fun save(models: List<RegionWithCountries>) {
        regionWithCountriesDao.insertRegionsWithCountries(
            models.map { it.currency },
            models.map { it.region },
            models.flatMap { it.countries })
    }

    override suspend fun findById(id: String): RegionWithCountries? = regionWithCountriesDao.region(id)
}