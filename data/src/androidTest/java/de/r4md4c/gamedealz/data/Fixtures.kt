package de.r4md4c.gamedealz.data

import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.Plain
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.Store

object Fixtures {

    fun region(code: String, currencyCode: String) = Region(code, currencyCode)

    fun country(code: String, region: Region) = Country(code, region.regionCode)

    fun currency(code: String = "currency") = Currency(code, "currency")

    fun plain(id: String = "id", shopId: String = "shopId") = Plain(id, shopId)

    fun store(name: String = "Steam", id: String = "steam") = Store(id, name, "")
}
