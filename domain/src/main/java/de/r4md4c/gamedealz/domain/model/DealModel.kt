package de.r4md4c.gamedealz.domain.model

typealias DealsResult = Pair<Int, List<DealModel>>

data class DealModel(
    val gameId: String,
    val title: String,
    val newPrice: Float,
    val oldPrice: Float,
    val priceCutPercentage: Short,
    val shop: ShopModel,
    val urls: Urls,
    val added: Long
)