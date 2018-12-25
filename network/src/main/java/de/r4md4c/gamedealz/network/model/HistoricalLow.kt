package de.r4md4c.gamedealz.network.model

import com.squareup.moshi.Json

data class HistoricalLow(
    val shop: Shop?,
    val price: Float?,
    @Json(name = "cut") val priceCutPercentage: Short?,
    val added: Long?
)
