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

import androidx.room.*
import de.r4md4c.gamedealz.data.entity.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

@Dao
internal interface StoresDao {

    /**
     * Inserts stores in a transaction.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stores: Collection<Store>)

    /**
     * Retrieves all stored stores.
     *
     * @return a list of stores.
     */
    @Query("SELECT * FROM Store")
    fun all(): Flow<List<Store>>

    /**
     * Retrieves all stored stores with ids
     *
     * @return a list of stores.
     */
    @Query("SELECT * FROM Store WHERE id IN (:ids)")
    fun all(ids: Set<String>): Flow<List<Store>>

    @Transaction
    fun replaceAll(stores: Collection<Store>) {
        delete()
        runBlocking { insert(stores) }
    }

    /**
     * All selected stores.
     *
     * @return a list of stores.
     */
    @Query("SELECT * FROM Store WHERE selected = 1")
    fun allSelected(): Flow<List<Store>>

    /**
     * Retrieves a single store.
     *
     * @param storeId the store id that will be used to retrieve the store.
     * @return the retrieved store or null if not found.
     */
    @Query("SELECT * FROM Store WHERE id = :storeId")
    suspend fun singleStore(storeId: String): Store?

    /**
     * Updates the selected field of a set of rows.
     *
     * @param selected The value of the selected column.
     * @param ids the ids of the items to be updated.
     * @return number of updated rows.
     */
    @Query("UPDATE Store SET selected=:selected WHERE id IN (:ids)")
    fun updateSelected(selected: Boolean, ids: Set<String>): Int

    @Query("DELETE FROM STORE")
    fun delete(): Int
}
