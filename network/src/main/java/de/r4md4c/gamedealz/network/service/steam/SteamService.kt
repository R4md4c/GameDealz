package de.r4md4c.gamedealz.network.service.steam

import de.r4md4c.gamedealz.network.model.steam.AppDetails
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
    ): Deferred<SteamResponse<AppDetails>>

    @GET("packagedetails")
    fun packageDetails(@Query("packageids") packageId: String): Deferred<SteamResponse<PackageDetails>>

}