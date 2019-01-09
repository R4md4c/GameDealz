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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import de.r4md4c.gamedealz.data.DATA
import de.r4md4c.gamedealz.data.GameDealzDatabase
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.data.entity.WatcheeStoreJoin
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.with
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest

class WatcheeStoreDaoTest : KoinTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val watcheeStoreJoinDao: WatcheeStoreJoinDao by inject()

    private val storesDao: StoresDao by inject()

    private val watchlistDao: WatchlistDao by inject()

    private val gameDealzDatabase: GameDealzDatabase by inject()

    @Before
    fun beforeEach() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        startKoin(listOf(DATA, module {
            single(override = true) {
                Room.inMemoryDatabaseBuilder(androidContext(), GameDealzDatabase::class.java).build()
            }
        })).with(context)
    }

    @After
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun insertJoins() {
        runBlocking {
            ArrangeBuilder()
            val storesList = storesList
            val watchlist = watcheesList
            val joins = arrayListOf<WatcheeStoreJoin>()
            watchlist.forEach { watchee ->
                storesList.forEach { store ->
                    joins.add(WatcheeStoreJoin(watchee.id, store.id))
                }
            }

            watcheeStoreJoinDao.insert(joins)
        }
    }

    @Test
    fun getStoresForWatchee() {
        runBlocking {
            val storesList = storesList
            val watchlist = watcheesList
            ArrangeBuilder()
                .withInsertedJoinInformation(watchlist, storesList)

            watchlist.forEach {
                val stores = watcheeStoreJoinDao.getStoresForWatchee(it.id)
                assertThat(stores).hasSize(10)
                assertThat(stores).containsAllIn(storesList)
            }

        }
    }

    @Test
    fun removingFromWatchlist_CascadeDelete() {
        runBlocking {
            val storesList = storesList
            val watchlist = watcheesList
            ArrangeBuilder()
                .withInsertedJoinInformation(watchlist, storesList)

            watchlistDao.delete(1)


            assertThat(watcheeStoreJoinDao.getStoresForWatchee(1)).isEmpty()
        }
    }

    private val watcheesList = (1..10).map {
        Watchee(
            id = it.toLong(),
            plainId = "plainId$it",
            title = "title:$it",
            currentPrice = 0f,
            dateAdded = 0,
            targetPrice = 0f,
            regionCode = "EU1",
            countryCode = "DE",
            currencyCode = "EUR"
        )
    }
    private val storesList = (1..10).map {
        Store(
            id = "storeId$it",
            name = "storeName$it",
            color = "color$it"
        )
    }

    inner class ArrangeBuilder {

        init {
            gameDealzDatabase.runInTransaction {
                runBlocking {
                    val storesList = storesList
                    val watchlist = watcheesList
                    storesDao.insert(storesList)
                    watchlistDao.insert(watchlist)
                }
            }
        }

        fun withInsertedJoinInformation(watchList: List<Watchee>, storesList: List<Store>) = apply {
            runBlocking {
                val joins = arrayListOf<WatcheeStoreJoin>()
                watchList.forEach { watchee ->
                    storesList.forEach { store ->
                        joins.add(WatcheeStoreJoin(watchee.id, store.id))
                    }
                }

                watcheeStoreJoinDao.insert(joins)
            }
        }
    }
}