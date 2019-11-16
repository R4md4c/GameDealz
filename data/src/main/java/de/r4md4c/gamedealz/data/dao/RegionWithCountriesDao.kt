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

package de.r4md4c.gamedealz.data.dao

import androidx.room.*
import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import kotlinx.coroutines.flow.Flow

@Dao
internal interface RegionWithCountriesDao {

    /**
     * Inserts a list of regions and countries in a transaction.
     *
     * @param regions the regions
     * @param countries the countries that needs to be linked with the region, make sure to supply am existing region.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegionsWithCountries(currencies: List<Currency>, regions: List<Region>, countries: List<Country>)

    /**
     * Retrieved all stored regions with their respective countries.
     *
     * @return a List of regions along their countries.
     */
    @Transaction
    @Query("SELECT * FROM Region LEFT JOIN Currency ON Region.fk_currencyCode = Currency.currencyCode")
    fun allRegions(): Flow<List<RegionWithCountries>>

    @Transaction
    @Query("SELECT * FROM Region LEFT JOIN Currency ON Region.fk_currencyCode = Currency.currencyCode WHERE Region.regionCode IN (:regionCodes)")
    fun allRegions(regionCodes: Set<String>): Flow<List<RegionWithCountries>>

    /**
     * Selects a region along with their countries by region.
     *
     * @param regionCode the region regionCode that this region will follow.
     * @return A single region along with its countries, otherwise null if the regionCode doesn't exist.
     */
    @Transaction
    @Query("SELECT * FROM Region LEFT JOIN Currency ON Region.fk_currencyCode = Currency.currencyCode WHERE regionCode = :regionCode")
    suspend fun region(regionCode: String): RegionWithCountries?
}
