package de.r4md4c.gamedealz.network.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Shop(val id: String, val name: String)

@JsonSerializable
data class Price(
    @Json(name = "price_new") val newPrice: Float,
    @Json(name = "price_old") val oldPrice: Float,
    @Json(name = "price_cut") val priceCutPercentage: Short,
    val url: String,
    val shop: Shop,
    val drm: Set<String>
)
