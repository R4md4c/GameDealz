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

import de.r4md4c.gamedealz.data.entity.Watchee
import kotlinx.coroutines.flow.Flow

interface WatchlistLocalDataSource : LocalDataSource<Watchee, Long> {

    /**
     * Removes a watched game by id.
     *
     * @return 1 if success 0 otherwise
     */
    suspend fun removeById(ids: Collection<Long>): Int

    /**
     * Removes a watched game by plain Id
     *
     * @return 1 if success 0 otherwise
     */
    suspend fun removeById(plainId: String): Int

    /**
     * Finds a single model by id.
     *
     * @param plainId the id that will be used to retrieve the model form.
     */
    fun findById(plainId: String): Flow<Watchee?>

    /**
     * Update the currentPrice and the lastChecked timestamp of a Watchee.
     *
     * @param id the id that of the watchee that you want to be updated.
     * @param lastFetchedPrice the lastFetchedPrice
     * @param lastFetchedStoreName the store name that the lastFetchedPrice was fetched from.
     * @param lastChecked the last checked timestamp
     * @return 1 if success else 0
     */
    suspend fun updateWatchee(id: Long, lastFetchedPrice: Float, lastFetchedStoreName: String, lastChecked: Long): Int

    /**
     * @return the most recent lastChecked in Seconds.
     */
    suspend fun mostRecentCheckDate(): Flow<Long>
}
