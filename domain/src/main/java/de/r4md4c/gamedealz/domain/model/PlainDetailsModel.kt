package de.r4md4c.gamedealz.domain.model

/**
 * A data class that aggregates the information retrieved from IsThereAnyDeal and Steam.
 *
 *  @param currencyModel the prices currency that this model has returned.
 *  @param plainId the plain id on IsThereAnyDeal
 *  @param shopPrices a Map between shop as keys, and a pair of its current price and it's Historical low price
 *  @param screenshots a list of image urls from the steam page.
 *  @param headerImage header image that is retrieved from the steam page.
 *  @param aboutGame Under the "About This Game" section on steam page.
 *  @param shortDescription the description that lies under the header image on steam.
 *  @param drmNotice The drm notice that is retrieved from steam.
 */
data class PlainDetailsModel(
    val currencyModel: CurrencyModel,
    val plainId: String,
    val shopPrices: Map<ShopModel, Pair<PriceModel, HistoricalLowModel?>>,
    val screenshots: List<String> = emptyList(),
    val headerImage: String? = null,
    val aboutGame: String? = null,
    val shortDescription: String? = null,
    val drmNotice: String? = null
)
