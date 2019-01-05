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
import de.r4md4c.gamedealz.data.dao.*
import de.r4md4c.gamedealz.data.entity.*

@Database(
    version = 2, entities = [Region::class,
        Country::class,
        Currency::class,
        Store::class,
        Plain::class,
        Watchee::class]
)
internal abstract class GameDealzDatabase : RoomDatabase() {

    abstract fun regionWithCountriesDao(): RegionWithCountriesDao

    abstract fun storesDao(): StoresDao

    abstract fun plainsDao(): PlainsDao

    abstract fun countriesDao(): CountriesDao

    abstract fun watchlistDao(): WatchlistDao

    companion object {
        const val DATABASE_NAME = "game_dealz.db"
    }
}
