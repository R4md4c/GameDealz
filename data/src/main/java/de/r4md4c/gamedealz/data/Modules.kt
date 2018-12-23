package de.r4md4c.gamedealz.data

import androidx.room.Room
import de.r4md4c.gamedealz.data.GameDealzDatabase.Companion.DATABASE_NAME
import de.r4md4c.gamedealz.data.repository.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val DATA = module {

    single {
        Room.databaseBuilder(androidContext(), GameDealzDatabase::class.java, DATABASE_NAME)
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

    factory<PlainsRepository> { PlainsLocalRepository(get()) }

    factory<RegionsRepository> { RegionLocalRepository(get()) }

    factory<StoresRepository> { StoresLocalRepository(get()) }

    factory<CountriesRepository> { CountriesLocalRepository(get()) }

}
