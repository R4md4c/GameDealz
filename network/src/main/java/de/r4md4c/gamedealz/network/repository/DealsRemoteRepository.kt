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

package de.r4md4c.gamedealz.network.repository

import de.r4md4c.gamedealz.network.model.Deal
import de.r4md4c.gamedealz.network.model.PageResult

interface DealsRemoteRepository {

    /**
     * Retrieve the current deals from the network.
     *
     * @param offset the offset to start the deals from.
     * @param limit the page ize
     * @param region the region code
     * @param country the country code
     * @param shops the shops ids to search for.
     */
    suspend fun deals(offset: Int, limit: Int, region: String, country: String, shops: Set<String>): PageResult<Deal>

}