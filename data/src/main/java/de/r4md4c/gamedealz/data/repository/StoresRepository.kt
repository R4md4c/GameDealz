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

import de.r4md4c.gamedealz.data.entity.Store
import kotlinx.coroutines.flow.Flow

interface StoresRepository : Repository<Store, String> {

    /**
     * Updates the stores according to the selected state.
     *
     * @param selected true or false
     * @param stores the stores to be updated.
     */
    fun updateSelected(selected: Boolean, stores: Set<Store>)

    /**
     * Gets the selected stores from the database.
     *
     * @return A collection of selected stores.
     */
    suspend fun selectedStores(): Flow<Collection<Store>>

    /**
     * Clears the repository then insert the stores.
     *
     * @param stores the stores that are going to be stored after clearing the table.
     */
    suspend fun replace(stores: Collection<Store>)
}
