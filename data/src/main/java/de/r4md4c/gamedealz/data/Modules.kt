package de.r4md4c.gamedealz.data

import androidx.room.Room
import de.r4md4c.gamedealz.data.GameDealzDatabase.Companion.DATABASE_NAME
import de.r4md4c.gamedealz.data.repository.RegionLocalRepository
import de.r4md4c.gamedealz.data.repository.RegionsRepository
import de.r4md4c.gamedealz.data.repository.StoresLocalRepository
import de.r4md4c.gamedealz.data.repository.StoresRepository
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

    factory<RegionsRepository> { RegionLocalRepository(get()) }

    factory<StoresRepository> { StoresLocalRepository(get()) }

}
