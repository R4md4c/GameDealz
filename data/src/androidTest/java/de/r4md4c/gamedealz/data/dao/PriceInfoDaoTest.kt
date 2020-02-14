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

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.r4md4c.gamedealz.data.Fixtures
import de.r4md4c.gamedealz.data.GameDealzDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PriceInfoDaoTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var priceInfoDao: PriceInfoDao

    private lateinit var plainsDao: PlainsDao

    private lateinit var storesDao: StoresDao

    private lateinit var database: GameDealzDatabase

    private val context
        get() = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun beforeEach() {
        database = Room.inMemoryDatabaseBuilder(context, GameDealzDatabase::class.java).build()
        priceInfoDao = database.priceInfoDao()
        plainsDao = database.plainsDao()
        storesDao = database.storesDao()
    }

    @After
    fun afterEach() {
        database.close()
    }

    @Test
    fun currentPrices_query_works() = runBlocking {
        val plainId1 = "id1"
        val plainId2 = "id2"
        plainsDao.insert(listOf(Fixtures.plain(id = plainId1), Fixtures.plain(id = plainId2)))
        storesDao.insert(
            listOf(
                Fixtures.store(id = "steam", name = "Steam"),
                Fixtures.store(id = "humblebundle", name = "Humble Bundle")
            )
        )
        val prices = listOf(
            Fixtures.price(plainId = plainId1, storeId = "steam"),
            Fixtures.price(plainId = plainId1, storeId = "humbleBundle")
        )
        val historicalLows = listOf(
                Fixtures.historicalLowPrice(plainId = plainId1, storeId = "steam"),
                Fixtures.historicalLowPrice(plainId = plainId1, storeId = "humbleBundle")
            )
        priceInfoDao.insertIntoPricesAndHistoricalLow(prices, historicalLows)


        val pricesInfo = priceInfoDao.currentPrices(plainId = plainId1)

        pricesInfo.size
        Unit
    }
}
