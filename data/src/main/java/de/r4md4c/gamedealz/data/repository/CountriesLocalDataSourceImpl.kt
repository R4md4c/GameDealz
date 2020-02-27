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

import de.r4md4c.gamedealz.data.dao.CountriesDao
import de.r4md4c.gamedealz.data.entity.Country
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CountriesLocalDataSourceImpl @Inject constructor(
    private val countriesDao: CountriesDao
) : CountriesLocalDataSource {

    override suspend fun allCountriesUnderRegion(regionCode: String): List<Country> =
        countriesDao.allCountriesUnderRegion(regionCode)

    override suspend fun all(ids: Collection<String>?): Flow<List<Country>> {
        throw UnsupportedOperationException("retrieving all countries is not supported")
    }

    override suspend fun save(models: List<Country>) {
        throw UnsupportedOperationException("save countries is not supported")
    }

    override suspend fun findById(id: String): Country? {
        throw UnsupportedOperationException("findById is not supported")
    }
}
