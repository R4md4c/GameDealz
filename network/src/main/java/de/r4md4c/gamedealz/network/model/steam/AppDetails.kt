package de.r4md4c.gamedealz.network.model.steam

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class AppDetails(
    val name: String,
    @Json(name = "steam_appid") val appId: String,
    val screenshots: List<Screenshot>,
    @Json(name = "header_image") val headerImage: String,
    @Json(name = "dlc") val dlcIds: List<Int>?,
    @Json(name = "website") val websiteUrl: String?,
    @Json(name = "about_the_game") val aboutGame: String?,
    @Json(name = "short_description") val shortDescription: String?,
    @Json(name = "drm_notice") val drmNotice: String?
)
