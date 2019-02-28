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

package de.r4md4c.gamedealz.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.data.entity.WatcheeStoreJoin
import kotlinx.coroutines.runBlocking

@Dao
internal interface WatcheeStoreJoinDao {

    @Insert
    suspend fun insert(joins: Collection<WatcheeStoreJoin>)

    @Query("SELECT * FROM Store INNER JOIN watchlist_store_join ON Store.id = watchlist_store_join.storeId WHERE watchlist_store_join.watcheeId=:watcheeId")
    suspend fun getStoresForWatchee(watcheeId: Long): List<Store>

    @Transaction
    fun saveWatcheeWithStores(watcheeDao: WatchlistDao, watchee: Watchee, stores: List<Store>) {
        runBlocking {
            val insertedId = watcheeDao.insert(watchee)
            stores.map { WatcheeStoreJoin(insertedId, it.id) }.run { insert(this) }
        }
    }
}
