package de.r4md4c.gamedealz.domain.model

import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import java.util.*

fun RegionWithCountries.findCountry(countryCode: String): Country? =
    countries.asSequence().firstOrNull { it.code.equals(countryCode, true) }

fun Country.displayName(): String =
    Locale("", code).displayCountry