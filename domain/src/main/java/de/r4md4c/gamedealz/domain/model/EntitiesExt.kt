package de.r4md4c.gamedealz.domain.model

import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.network.model.*
import java.text.NumberFormat
import java.util.*

internal fun RegionWithCountriesModel.findCountry(countryCode: String): CountryModel? =
    countries.asSequence().firstOrNull { it.code.equals(countryCode, true) }

internal fun RegionWithCountries.toModel(): RegionWithCountriesModel = RegionWithCountriesModel(region.regionCode,
    currency.toCurrencyModel(),
    countries.map { it.toCountryModel() })

fun CountryModel.displayName(): String =
    Locale("", code).displayCountry

internal fun Currency.toCurrencyModel(): CurrencyModel = CurrencyModel(this.currencyCode, this.sign)

internal fun Country.toCountryModel(): CountryModel = CountryModel(code)

internal fun Deal.toDealModel(currencyModel: CurrencyModel): DealModel =
    DealModel(
        gameId,
        title,
        newPrice,
        oldPrice,
        priceCutPercentage,
        shop.toShopModel(),
        urls.toUrls(),
        added,
        currencyModel
    )

internal fun Shop.toShopModel(): ShopModel = ShopModel(id, name)

internal fun GameUrls.toUrls(): Urls = Urls(buy, gameInfo)

internal fun Store.toStoreModel(): StoreModel = StoreModel(id, name, selected)

internal fun Price.toPriceModel(): PriceModel = PriceModel(newPrice, oldPrice, priceCutPercentage, url, shop, drm)

internal fun HistoricalLow.toModel(): HistoricalLowModel? {
    val shop = shop ?: return null
    val price = price ?: return null
    val priceCutPercentage = priceCutPercentage ?: return null
    val added = added ?: return null

    return HistoricalLowModel(shop.toShopModel(), price, priceCutPercentage, added)
}

fun Float.formatCurrency(currencyModel: CurrencyModel): String? =
    runCatching {
        val format = NumberFormat.getCurrencyInstance()
        val currency = java.util.Currency.getInstance(currencyModel.currencyCode)
        format.currency = currency

        format.format(this)
    }.getOrNull()