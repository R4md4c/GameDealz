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

import de.r4md4c.gamedealz.network.model.Store

interface StoresRemoteRepository {

    /**
     * Retrieves the stores from the network.
     * You should always specify region and country to narrow down the search.
     *
     * @param region the region.
     * @param country the country could be null as well.
     * @return a List of stores, under this region and country.
     */
    suspend fun stores(region: String, country: String? = null): List<Store>
}