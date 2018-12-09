package de.r4md4c.gamedealz.data

import androidx.room.Database
import androidx.room.RoomDatabase
import de.r4md4c.gamedealz.data.dao.RegionWithCountriesDao
import de.r4md4c.gamedealz.data.dao.StoresDao
import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.Store

@Database(version = 1, entities = [Region::class, Country::class, Currency::class, Store::class])
internal abstract class GameDealzDatabase : RoomDatabase() {

    abstract fun regionWithCountriesDao(): RegionWithCountriesDao

    abstract fun storesDao(): StoresDao

    companion object {
        const val DATABASE_NAME = "game_dealz.db"
    }
}
