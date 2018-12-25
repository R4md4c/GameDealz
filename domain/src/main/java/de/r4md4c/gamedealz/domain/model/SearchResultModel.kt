package de.r4md4c.gamedealz.domain.model

data class SearchResultModel(
    val title: String,
    val gameId: String,
    val prices: List<PriceModel>,
    val historicalLow: HistoricalLowModel?
)
