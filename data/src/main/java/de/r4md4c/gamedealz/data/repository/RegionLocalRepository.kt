package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.dao.RegionWithCountriesDao
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.reactive.openSubscription

internal class RegionLocalRepository(private val regionWithCountriesDao: RegionWithCountriesDao) : RegionsRepository {

    override suspend fun all(ids: Collection<String>?): ReceiveChannel<List<RegionWithCountries>> =
        (ids?.let { regionWithCountriesDao.allRegions(it.toSet()) } ?: regionWithCountriesDao.allRegions())
            .onBackpressureLatest()
            .distinctUntilChanged()
            .openSubscription()

    override suspend fun save(models: List<RegionWithCountries>) {
        regionWithCountriesDao.insertRegionsWithCountries(
            models.map { it.currency },
            models.map { it.region },
            models.flatMap { it.countries })
    }

    override suspend fun findById(id: String): RegionWithCountries? = regionWithCountriesDao.region(id)
}