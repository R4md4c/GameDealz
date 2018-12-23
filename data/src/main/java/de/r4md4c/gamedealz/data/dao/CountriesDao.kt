package de.r4md4c.gamedealz.data.dao

import androidx.room.Dao
import androidx.room.Query
import de.r4md4c.gamedealz.data.entity.Country

@Dao
interface CountriesDao {

    @Query("SELECT * FROM Country WHERE regionCode = :regionCode")
    suspend fun allCountriesUnderRegion(regionCode: String): List<Country>

}
