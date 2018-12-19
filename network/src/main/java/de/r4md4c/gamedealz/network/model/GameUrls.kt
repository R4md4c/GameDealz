package de.r4md4c.gamedealz.network.model

import com.squareup.moshi.Json

data class GameUrls(val buy: String, @Json(name = "game") val gameInfo: String)