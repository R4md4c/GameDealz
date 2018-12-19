package de.r4md4c.gamedealz.network.model

import com.squareup.moshi.Json

data class Deal(
    @Json(name = "plain") val gameId: String,
    val title: String,
    @Json(name = "price_new") val newPrice: Float,
    @Json(name = "price_old") val oldPrice: Float,
    @Json(name = "price_cut") val priceCutPercentage: Short,
    val added: Long,
    val shop: Shop,
    val drm: Set<String>,
    val urls: GameUrls
)
