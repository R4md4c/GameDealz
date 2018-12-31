/*
 * This file is part of GameDealz.
 *
 * GameDealz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * GameDealz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GameDealz.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.dao.RegionWithCountriesDao
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.reactive.openSubscription

internal class RegionLocalRepository(private val regionWithCountriesDao: RegionWithCountriesDao) : RegionsRepository {

    override suspend fun all(ids: Collection<String>?): ReceiveChannel<List<RegionWithCountries>> =
        (ids?.let { regionWithCountriesDao.allRegions(it.toSet()) } ?: regionWithCountriesDao.allRegions())
            .onBackpressureLatest()
            .openSubscription()

    override suspend fun save(models: List<RegionWithCountries>) {
        regionWithCountriesDao.insertRegionsWithCountries(
            models.map { it.currency },
            models.map { it.region },
            models.flatMap { it.countries })
    }

    override suspend fun findById(id: String): RegionWithCountries? = regionWithCountriesDao.region(id)
}