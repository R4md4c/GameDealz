package de.r4md4c.gamedealz.domain.model

import de.r4md4c.gamedealz.network.model.Shop

data class PriceModel(
    val newPrice: Float,
    val oldPrice: Float,
    val priceCutPercentage: Short,
    val url: String,
    val shop: Shop,
    val drm: Set<String>
)