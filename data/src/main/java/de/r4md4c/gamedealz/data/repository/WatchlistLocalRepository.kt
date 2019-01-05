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

import de.r4md4c.gamedealz.data.dao.WatchlistDao
import de.r4md4c.gamedealz.data.entity.Watchee
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.reactive.openSubscription

class WatchlistLocalRepository(private val watchlistDao: WatchlistDao) : WatchlistRepository {

    override suspend fun findById(plainId: String): Watchee? = watchlistDao.findOne(plainId)

    override suspend fun all(ids: Collection<Long>?): ReceiveChannel<List<Watchee>> =
        (ids?.let { watchlistDao.findAll(it) } ?: watchlistDao.findAll()).openSubscription()

    override suspend fun removeById(id: Long): Int = watchlistDao.delete(id)

    override suspend fun save(models: List<Watchee>) = watchlistDao.insert(models)

    override suspend fun findById(id: Long): Watchee? = watchlistDao.findOne(id)

}