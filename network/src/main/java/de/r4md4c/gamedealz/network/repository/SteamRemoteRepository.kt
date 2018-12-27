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