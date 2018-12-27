package de.r4md4c.gamedealz.domain.model

/**
 * A data class that aggregates the information retrieved from IsThereAnyDeal and Steam.
 *
 *  @param plainId the plain id on IsThereAnyDeal
 *  @param prices a list of prices from several shops.
 *  @param screenshots a list of image urls from the steam page.
 *  @param headerImage header image that is retrieved from the steam page.
 *  @param aboutGame Under the "About This Game" section on steam page.
 *  @param shortDescription the description that lies under the header image on steam.
 *  @param drmNotice The drm notice that is retreived from steam.
 */
data class PlainDetailsModel(
    val plainId: String,
    val prices: List<PriceModel>,
    val screenshots: List<String> = emptyList(),
    val headerImage: String? = null,
    val aboutGame: String? = null,
    val shortDescription: String? = null,
    val drmNotice: String? = null
)
