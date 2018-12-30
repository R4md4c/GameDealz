package de.r4md4c.gamedealz.network.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class HistoricalLow(
    val shop: Shop?,
    val price: Float?,
    @Json(name = "cut") val priceCutPercentage: Short?,
    val added: Long?
)
