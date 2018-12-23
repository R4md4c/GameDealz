package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.dao.CountriesDao
import de.r4md4c.gamedealz.data.entity.Country
import kotlinx.coroutines.channels.ReceiveChannel

class CountriesLocalRepository(private val countriesDao: CountriesDao) : CountriesRepository {

    override suspend fun allCountriesUnderRegion(regionCode: String): List<Country> =
        countriesDao.allCountriesUnderRegion(regionCode)

    override suspend fun all(ids: Collection<String>?): ReceiveChannel<List<Country>> {
        throw UnsupportedOperationException("retrieving all countries is not supported")
    }

    override suspend fun save(models: List<Country>) {
        throw UnsupportedOperationException("save countries is not supported")
    }

    override suspend fun findById(id: String): Country? {
        throw UnsupportedOperationException("findById is not supported")
    }
}