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

import de.r4md4c.gamedealz.data.dao.PlainsDao
import de.r4md4c.gamedealz.data.entity.Plain
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class PlainsLocalRepository @Inject constructor(
    private val plainsDao: PlainsDao
) : PlainsRepository {

    override suspend fun all(ids: Collection<String>?): Flow<List<Plain>> {
        throw UnsupportedOperationException("PlainsDao doesn't support retrieving full list")
    }

    override suspend fun count(): Int = plainsDao.count()

    override suspend fun save(models: List<Plain>) {
        plainsDao.insert(models)
    }

    override suspend fun findById(id: String): Plain? = plainsDao.findOne(id)
}
