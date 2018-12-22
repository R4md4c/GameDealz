package de.r4md4c.gamedealz.data

import androidx.room.Database
import androidx.room.RoomDatabase
import de.r4md4c.gamedealz.data.dao.PlainsDao
import de.r4md4c.gamedealz.data.dao.RegionWithCountriesDao
import de.r4md4c.gamedealz.data.dao.StoresDao
import de.r4md4c.gamedealz.data.entity.*

@Database(version = 1, entities = [Region::class, Country::class, Currency::class, Store::class, Plain::class])
internal abstract class GameDealzDatabase : RoomDatabase() {

    abstract fun regionWithCountriesDao(): RegionWithCountriesDao

    abstract fun storesDao(): StoresDao

    abstract fun plainsDao(): PlainsDao

    companion object {
        const val DATABASE_NAME = "game_dealz.db"
    }
}
