package de.r4md4c.gamedealz.network.model.steam

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Screenshot(
    @Json(name = "path_thumbnail") val thumbnail: String,
    @Json(name = "path_full") val full: String
)
