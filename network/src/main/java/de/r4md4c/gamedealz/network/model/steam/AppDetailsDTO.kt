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

package de.r4md4c.gamedealz.network.model.steam

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppDetailsDTO(
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
