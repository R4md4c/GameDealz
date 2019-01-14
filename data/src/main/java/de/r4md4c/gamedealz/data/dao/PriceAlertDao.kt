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
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.r4md4c.gamedealz.data.entity.PriceAlert
import io.reactivex.Flowable

@Dao
interface PriceAlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(priceAlerts: List<PriceAlert>)

    @Query("SELECT COUNT(*) FROM PriceAlert")
    fun unreadCount(): Flowable<Int>

    @Query("SELECT * FROM PriceAlert")
    fun findAll(): Flowable<List<PriceAlert>>

    @Query("SELECT * FROM PriceAlert WHERE id IN (:ids)")
    fun findAll(ids: Collection<Long>): Flowable<List<PriceAlert>>

    @Query("SELECT * FROM PriceAlert WHERE watcheeId = :watcheeId")
    suspend fun findByWatcheeId(watcheeId: Long): PriceAlert?

    @Query("DELETE FROM PriceAlert WHERE id=:id")
    fun delete(id: Long): Int

    @Query("DELETE FROM PriceAlert WHERE watcheeId=:watcheeId")
    fun deleteByWatcheeId(watcheeId: Long): Int

}
