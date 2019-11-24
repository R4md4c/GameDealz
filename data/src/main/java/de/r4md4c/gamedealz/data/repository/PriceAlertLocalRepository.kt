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

import de.r4md4c.gamedealz.data.dao.PriceAlertDao
import de.r4md4c.gamedealz.data.entity.PriceAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

internal class PriceAlertLocalRepository(private val priceAlertDao: PriceAlertDao) : PriceAlertRepository {

    override suspend fun all(ids: Collection<Long>?): Flow<List<PriceAlert>> =
        (ids?.let { priceAlertDao.findAll(ids) } ?: priceAlertDao.findAll()).distinctUntilChanged()

    override suspend fun findByWatcheeId(watcheeId: Long): PriceAlert? =
        priceAlertDao.findByWatcheeId(watcheeId)

    override suspend fun remove(id: Long) = priceAlertDao.delete(id)

    override suspend fun removeByWatcheeId(watcheeId: Long): Int = priceAlertDao.deleteByWatcheeId(watcheeId)

    override fun unreadCount(): Flow<Int> =
        priceAlertDao.unreadCount().distinctUntilChanged()

    override suspend fun save(models: List<PriceAlert>) {
        priceAlertDao.insert(models)
    }

    override suspend fun findById(id: Long): PriceAlert? =
        throw UnsupportedOperationException("findById is not supported in this repository")
}
