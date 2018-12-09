package de.r4md4c.gamedealz.network.model

import com.squareup.moshi.Json

data class Shop(val id: String, val name: String)

data class Price(
    @Json(name = "price_new") val newPrice: Float,
    @Json(name = "price_old") val oldPrice: Float,
    @Json(name = "price_cut") val priceCutPercentage: Short,
    val url: String,
    val shop: Shop,
    val drm: Set<String>
)
