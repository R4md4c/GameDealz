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
import de.r4md4c.gamedealz.data.entity.Watchee
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM Watchlist WHERE plainId = :plainId")
    fun findOne(plainId: String): Flow<List<Watchee>>

    @Query("SELECT * FROM Watchlist WHERE id = :id")
    suspend fun findOne(id: Long): Watchee?

    @Query("SELECT * FROM Watchlist ORDER BY dateAdded DESC")
    fun findAll(): Flow<List<Watchee>>

    @Query("SELECT * FROM Watchlist WHERE id IN (:ids) ORDER BY dateAdded DESC")
    fun findAll(ids: Collection<Long>): Flow<List<Watchee>>

    @Query("DELETE FROM Watchlist WHERE id = :id")
    fun delete(id: Long): Int

    @Query("DELETE FROM Watchlist WHERE id IN (:ids)")
    fun delete(ids: Collection<Long>): Int

    @Query("DELETE FROM Watchlist WHERE plainId = :plainId")
    fun delete(plainId: String): Int

    @Insert
    suspend fun insert(watchees: List<Watchee>)

    @Insert
    suspend fun insert(watchee: Watchee): Long

    @Query("UPDATE Watchlist SET lastFetchedPrice = :lastFetchedPrice, lastCheckDate = :lastChecked, lastFetchedStoreName = :lastFetchedStoreName WHERE id = :id")
    fun updateWatchee(id: Long, lastFetchedPrice: Float, lastFetchedStoreName: String, lastChecked: Long): Int

    @Query("SELECT COALESCE(MAX(lastCheckDate), 0) FROM Watchlist")
    fun mostRecentLastCheckDate(): Flow<Long>

}
