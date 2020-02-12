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

package de.r4md4c.gamedealz.network.service.steam

import de.r4md4c.gamedealz.network.model.steam.AppDetailsDTO
import de.r4md4c.gamedealz.network.model.steam.PackageDetails
import de.r4md4c.gamedealz.network.model.steam.ResponseWrapper
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

typealias SteamResponse<T> = Map<String, ResponseWrapper<T>>

/**
 * A retrofit interface that is used to access Steam API.
 */
internal interface SteamService {

    /**
     * Retrieve the app details of an app id.
     *
     * @param appId the app id that you'd want to retrieve.
     * @param filters primarily used to get a specific view of the full json. Currently we're only interested in the
     * header image and the screenshots. (Note: header_image filter doesn't work.)
     */
    @GET("appdetails")
    fun appDetails(
        @Query("appids") appId: String,
        @Query("filters") filters: String = "basic,screenshots"
    ): Deferred<SteamResponse<AppDetailsDTO>>

    @GET("packagedetails")
    fun packageDetails(@Query("packageids") packageId: String): Deferred<SteamResponse<PackageDetails>>
}
