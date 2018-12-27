package de.r4md4c.gamedealz.network.model.steam

import com.squareup.moshi.Json

data class Screenshot(
    @Json(name = "path_thumbnail") val thumbnail: String,
    @Json(name = "path_full") val full: String
)
