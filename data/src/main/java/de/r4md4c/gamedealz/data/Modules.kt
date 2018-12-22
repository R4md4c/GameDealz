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

    single {
        get<GameDealzDatabase>().regionWithCountriesDao()
    }

    single {
        get<GameDealzDatabase>().storesDao()
    }

    single {
        get<GameDealzDatabase>().plainsDao()
    }

    single<PlainsRepository> { PlainsLocalRepository(get()) }

    single<RegionsRepository> { RegionLocalRepository(get()) }

    single<StoresRepository> { StoresLocalRepository(get()) }

}
