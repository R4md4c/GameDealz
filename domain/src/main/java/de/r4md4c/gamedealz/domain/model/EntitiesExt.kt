package de.r4md4c.gamedealz.domain.model

import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.network.model.Deal
import de.r4md4c.gamedealz.network.model.GameUrls
import de.r4md4c.gamedealz.network.model.Shop
import java.text.NumberFormat
import java.util.*

internal fun RegionWithCountries.findCountry(countryCode: String): Country? =
    countries.asSequence().firstOrNull { it.code.equals(countryCode, true) }

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

fun Float.formatCurrency(currencyModel: CurrencyModel): String? =
    runCatching {
        val format = NumberFormat.getCurrencyInstance()
        val currency = java.util.Currency.getInstance(currencyModel.currencyCode)
        format.currency = currency

        format.format(this)
    }.getOrNull()