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

import de.r4md4c.gamedealz.network.model.steam.AppDetails
import de.r4md4c.gamedealz.network.model.steam.PackageDetails

interface SteamRemoteRepository {

    /**
     * Gets appDetails from steam.
     *
     * @param appId the app id that you want its details.
     * @return The app details, null if the response was not having success = true.
     */
    suspend fun appDetails(appId: String): AppDetails?

    /**
     * Gets packageDetails from steam.
     *
     * @param packageId the package id that you want its details.
     * @return The package details, null if the response was not having success = true.
     */
    suspend fun packageDetails(packageId: String): PackageDetails?
}