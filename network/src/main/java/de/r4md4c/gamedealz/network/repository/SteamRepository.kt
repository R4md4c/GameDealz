package de.r4md4c.gamedealz.network.repository

import de.r4md4c.gamedealz.network.model.steam.AppDetails
import de.r4md4c.gamedealz.network.model.steam.PackageDetails
import de.r4md4c.gamedealz.network.service.steam.SteamService

internal class SteamRepository(private val steamService: SteamService) : SteamRemoteRepository {

    override suspend fun appDetails(appId: String): AppDetails? =
        steamService.appDetails(appId).await().run {
            get(appId)?.takeIf { it.success }?.data
        }

    override suspend fun packageDetails(packageId: String): PackageDetails? =
        steamService.packageDetails(packageId).await().run {
            get(packageId)?.takeIf { it.success }?.data
        }
}