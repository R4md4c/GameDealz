package de.r4md4c.gamedealz.data.dao

import androidx.room.*
import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.RegionWithCountries

@Dao
internal interface RegionWithCountriesDao {

    /**
     * Inserts a list of regions and countries in a transaction.
     *
     * @param regions the regions
     * @param countries the countries that needs to be linked with the region, make sure to supply am existing region.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegionsWithCountries(regions: List<Region>, countries: List<Country>)

    /**
     * Retrieved all stored regions with their respective countries.
     *
     * @return a List of regions along their countries.
     */
    @Transaction
    @Query("SELECT * FROM Region")
    suspend fun allRegions(): List<RegionWithCountries>

    /**
     * Selects a region along with their countries by region.
     *
     * @param regionCode the region code that this region will follow.
     * @return A single region along with its countries, otherwise null if the regionCode doesn't exist.
     */
    @Transaction
    @Query("SELECT * FROM Region WHERE code = :regionCode")
    suspend fun region(regionCode: String): RegionWithCountries?
}
