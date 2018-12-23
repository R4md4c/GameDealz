package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.entity.Country

interface CountriesRepository : Repository<Country, String> {

    suspend fun allCountriesUnderRegion(regionCode: String): List<Country>
}
