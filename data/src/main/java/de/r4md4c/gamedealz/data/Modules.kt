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

import androidx.room.Room
import de.r4md4c.gamedealz.data.GameDealzDatabase.Companion.DATABASE_NAME
import de.r4md4c.gamedealz.data.migrations.MIGRATION_1_2
import de.r4md4c.gamedealz.data.repository.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val DATA = module {

    single {
        Room.databaseBuilder(androidContext(), GameDealzDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    factory {
        get<GameDealzDatabase>().regionWithCountriesDao()
    }

    factory {
        get<GameDealzDatabase>().storesDao()
    }

    factory {
        get<GameDealzDatabase>().plainsDao()
    }

    factory {
        get<GameDealzDatabase>().countriesDao()
    }

    factory {
        get<GameDealzDatabase>().watchlistDao()
    }
    factory {
        get<GameDealzDatabase>().watcheeStoreJoinDao()
    }

    factory<WatchlistRepository> { WatchlistLocalRepository(get(), get()) }

    factory<WatchlistStoresRepository> { WatchlistLocalRepository(get(), get()) }

    factory<PlainsRepository> { PlainsLocalRepository(get()) }

    factory<RegionsRepository> { RegionLocalRepository(get()) }

    factory<StoresRepository> { StoresLocalRepository(get()) }

    factory<CountriesRepository> { CountriesLocalRepository(get()) }

}
