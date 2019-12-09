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

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.r4md4c.gamedealz.data.GameDealzDatabase
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.data.entity.WatcheeStoreJoin
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WatcheeStoreDaoTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context
        get() = ApplicationProvider.getApplicationContext<Context>()

    private lateinit var watcheeStoreJoinDao: WatcheeStoreJoinDao

    private lateinit var storesDao: StoresDao

    private lateinit var watchlistDao: WatchlistDao

    private lateinit var gameDealzDatabase: GameDealzDatabase

    @Before
    fun beforeEach() {
        gameDealzDatabase =
            Room.inMemoryDatabaseBuilder(context, GameDealzDatabase::class.java).build()

        watcheeStoreJoinDao = gameDealzDatabase.watcheeStoreJoinDao()
        storesDao = gameDealzDatabase.storesDao()
        watchlistDao = gameDealzDatabase.watchlistDao()
    }

    @After
    fun afterEach() {
        gameDealzDatabase.close()
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
                assertThat(stores).containsAll(storesList)
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
            lastFetchedPrice = 0f,
            lastFetchedStoreName = "",
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