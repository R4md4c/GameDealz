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

package de.r4md4c.gamedealz.data

import androidx.room.Database
import androidx.room.RoomDatabase
import de.r4md4c.gamedealz.data.dao.CountriesDao
import de.r4md4c.gamedealz.data.dao.PlainsDao
import de.r4md4c.gamedealz.data.dao.PriceAlertDao
import de.r4md4c.gamedealz.data.dao.RegionWithCountriesDao
import de.r4md4c.gamedealz.data.dao.StoresDao
import de.r4md4c.gamedealz.data.dao.WatcheeStoreJoinDao
import de.r4md4c.gamedealz.data.dao.WatchlistDao
import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.Plain
import de.r4md4c.gamedealz.data.entity.PriceAlert
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.data.entity.WatcheeStoreJoin

@Database(
    version = 3, entities = [Region::class,
        Country::class,
        Currency::class,
        Store::class,
        Plain::class,
        Watchee::class,
        WatcheeStoreJoin::class,
        PriceAlert::class]
)
internal abstract class GameDealzDatabase : RoomDatabase() {

    abstract fun regionWithCountriesDao(): RegionWithCountriesDao

    abstract fun storesDao(): StoresDao

    abstract fun plainsDao(): PlainsDao

    abstract fun countriesDao(): CountriesDao

    abstract fun watchlistDao(): WatchlistDao

    abstract fun watcheeStoreJoinDao(): WatcheeStoreJoinDao

    abstract fun priceAlertDao(): PriceAlertDao

    companion object {
        const val DATABASE_NAME = "game_dealz.db"
    }
}
