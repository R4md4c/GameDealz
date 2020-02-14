package de.r4md4c.gamedealz.data

import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.HistoricalLowPrice
import de.r4md4c.gamedealz.data.entity.Plain
import de.r4md4c.gamedealz.data.entity.Price
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.Store

object Fixtures {

    fun region(code: String, currencyCode: String) = Region(code, currencyCode)

    fun country(code: String, region: Region) = Country(code, region.regionCode)

    fun currency(code: String = "currency") = Currency(code, "currency")

    fun plain(id: String = "id", shopId: String = "shopId") = Plain(id, shopId)

    fun store(name: String = "Steam", id: String = "steam") = Store(id, name, "")

    fun price(plainId: String, storeId: String) = Price(
        plainId,
        storeId,
        newPrice = 0f,
        oldPrice = 0f,
        priceCutPercentage = 0,
        dateCreated = 0,
        dateUpdated = 0
    )

    fun historicalLowPrice(plainId: String, storeId: String) = HistoricalLowPrice(
        plainId,
        storeId,
        price = 0f,
        priceCutPercentage = 0,
        priceDate = 0,
        dateCreated = 0,
        dateUpdated = 0
    )
}
