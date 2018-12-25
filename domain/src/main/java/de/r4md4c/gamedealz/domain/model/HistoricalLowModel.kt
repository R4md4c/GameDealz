package de.r4md4c.gamedealz.domain.model

data class HistoricalLowModel(
    val shop: ShopModel,
    val price: Float,
    val priceCutPercentage: Short,
    val added: Long
)
