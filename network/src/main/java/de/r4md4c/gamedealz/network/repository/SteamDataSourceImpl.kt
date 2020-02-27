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

import de.r4md4c.gamedealz.network.model.steam.AppDetailsDTO
import de.r4md4c.gamedealz.network.model.steam.PackageDetails
import de.r4md4c.gamedealz.network.service.steam.SteamService
import javax.inject.Inject

internal class SteamDataSourceImpl @Inject constructor(
    private val steamService: SteamService
) : SteamRemoteDataSource {

    override suspend fun appDetails(appId: String): AppDetailsDTO? =
        steamService.appDetails(appId).await().run {
            get(appId)?.takeIf { it.success }?.data
        }

    override suspend fun packageDetails(packageId: String): PackageDetails? =
        steamService.packageDetails(packageId).await().run {
            get(packageId)?.takeIf { it.success }?.data
        }
}
