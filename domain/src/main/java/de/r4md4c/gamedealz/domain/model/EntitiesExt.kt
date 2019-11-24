/*
 * This file is part of GameDealz.
 *
 * GameDealz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * GameDealz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GameDealz.  If not, see <https://www.gnu.org/licenses/>.
 */
@file:Suppress("TooManyFunctions")

package de.r4md4c.gamedealz.domain.model

import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.domain.cache.NumberFormatCurrencyCache
import de.r4md4c.gamedealz.network.model.Deal
import de.r4md4c.gamedealz.network.model.GameUrls
import de.r4md4c.gamedealz.network.model.HistoricalLow
import de.r4md4c.gamedealz.network.model.Price
import de.r4md4c.gamedealz.network.model.Shop
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

internal fun Deal.toDealModel(currencyModel: CurrencyModel, colorRgb: String): DealModel =
    DealModel(
        gameId,
        title,
        newPrice,
        oldPrice,
        priceCutPercentage,
        shop.toShopModel(colorRgb),
        urls.toUrls(),
        added,
        drm,
        currencyModel
    )

internal fun Shop.toShopModel(rgbColor: String): ShopModel = ShopModel(id, name, rgbColor)

internal fun GameUrls.toUrls(): Urls = Urls(buy, gameInfo)

internal fun Store.toStoreModel(): StoreModel = StoreModel(id, name, selected)

internal fun Price.toPriceModel(storeColor: String): PriceModel =
    PriceModel(newPrice, oldPrice, priceCutPercentage, url, shop.toShopModel(storeColor), drm)

fun DealModel.toPriceModel(): PriceModel =
    PriceModel(newPrice, oldPrice, priceCutPercentage, urls.buyUrl, shop, drm)

internal fun HistoricalLow.toModel(colorRgb: String): HistoricalLowModel? {
    val shop = shop ?: return null
    val price = price ?: return null
    val priceCutPercentage = priceCutPercentage ?: return null
    val added = added ?: return null

    return HistoricalLowModel(shop.toShopModel(colorRgb), price, priceCutPercentage, added)
}

fun Float.formatCurrency(currencyModel: CurrencyModel): String? =
    runCatching {
        NumberFormatCurrencyCache[currencyModel.currencyCode].format(this)
    }.getOrNull()
