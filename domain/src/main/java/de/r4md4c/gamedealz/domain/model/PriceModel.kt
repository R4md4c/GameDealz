package de.r4md4c.gamedealz.domain.model

data class PriceModel(
    val newPrice: Float,
    val oldPrice: Float,
    val priceCutPercentage: Short,
    val url: String,
    val shop: ShopModel,
    val drm: Set<String>
)