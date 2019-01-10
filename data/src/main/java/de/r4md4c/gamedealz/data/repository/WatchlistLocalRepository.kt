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

import de.r4md4c.gamedealz.data.dao.WatcheeStoreJoinDao
import de.r4md4c.gamedealz.data.dao.WatchlistDao
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.data.entity.WatcheeWithStores
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.reactive.openSubscription

internal class WatchlistLocalRepository(
    private val watchlistDao: WatchlistDao,
    private val watchlistStoresDao: WatcheeStoreJoinDao
) : WatchlistRepository, WatchlistStoresRepository {

    override suspend fun findById(plainId: String): ReceiveChannel<Watchee?> =
        watchlistDao.findOne(plainId).openSubscription().map { it.firstOrNull() }

    override suspend fun all(ids: Collection<Long>?): ReceiveChannel<List<Watchee>> =
        (ids?.let { watchlistDao.findAll(it) } ?: watchlistDao.findAll()).openSubscription()

    override suspend fun removeById(id: Long): Int = watchlistDao.delete(id)

    override suspend fun removeById(plainId: String): Int = watchlistDao.delete(plainId)

    override suspend fun save(models: List<Watchee>) = watchlistDao.insert(models)

    override suspend fun findById(id: Long): Watchee? = watchlistDao.findOne(id)

    override suspend fun findWatcheeWithStores(watchee: Watchee): WatcheeWithStores? {
        val retrievedWatchee = findById(watchee.id) ?: return null
        return WatcheeWithStores(
            retrievedWatchee,
            watchlistStoresDao.getStoresForWatchee(watcheeId = retrievedWatchee.id).toSet()
        )
    }

    override suspend fun allWatcheesWithStores(): List<WatcheeWithStores> =
        all().first().mapNotNull {
            WatcheeWithStores(it, watchlistStoresDao.getStoresForWatchee(watcheeId = it.id).toSet())
        }

    override suspend fun saveWatcheeWithStores(watchee: Watchee, stores: List<Store>) {
        watchlistStoresDao.saveWatcheeWithStores(watchlistDao, watchee, stores)
    }

    override suspend fun updateWatchee(id: Long, currentPrice: Float, lastChecked: Long): Int =
        watchlistDao.updateWatchee(id, currentPrice, lastChecked)
}