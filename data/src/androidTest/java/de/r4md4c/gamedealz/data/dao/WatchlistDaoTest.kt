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

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import de.r4md4c.gamedealz.data.DATA
import de.r4md4c.gamedealz.data.GameDealzDatabase
import de.r4md4c.gamedealz.data.entity.Watchee
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
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

class WatchlistDaoTest : KoinTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val watchlistDao: WatchlistDao by inject()

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
    fun insert() {
        runBlocking {
            watchlistDao.insert(watcheesList)
        }
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertAbortsTransactionWhenDuplicateErrors() {
        runBlocking {
            watchlistDao.insert(watcheesList)

            watchlistDao.insert(listOf(watcheesList[1]))
        }
    }

    @Test
    fun findOne_findsReallyOne() {
        runBlocking {
            watchlistDao.insert(watcheesList)

            assertThat(watchlistDao.findOne("plainId5").awaitFirstOrNull()).isNotEmpty()
        }
    }

    @Test
    fun update() {
        runBlocking {
            watchlistDao.insert(watcheesList)

            val watchee = watchlistDao.findOne("plainId5").awaitFirst().first()

            val timeStamp = System.currentTimeMillis()
            assertThat(watchlistDao.updateWatchee(watchee.id, 500f, "store", timeStamp)).isEqualTo(1)
            assertThat(watchlistDao.findOne("plainId5").awaitFirst().first()).isEqualTo(
                watchee.copy(
                    lastFetchedPrice = 500f,
                    lastCheckDate = timeStamp
                )
            )
        }
    }

    @Test
    fun removeByPlainId() {
        runBlocking {
            watchlistDao.insert(watcheesList.first())

            assertThat(watchlistDao.delete("plainId1")).isEqualTo(1)
        }
    }

    @Test
    fun removeById() {
        runBlocking {
            watchlistDao.insert(watcheesList.first())

            assertThat(watchlistDao.delete(1)).isEqualTo(1)
        }
    }

    @Test
    fun mostRecentLastCheckDate() {
        runBlocking {
            watchlistDao.insert(watcheesList.mapIndexed { index: Int, watchee: Watchee -> watchee.copy(lastCheckDate = index.toLong() + 1) })

            watchlistDao.mostRecentLastCheckDate().test().assertValue(10)
        }
    }
    private val watcheesList = (1..10).map {
        Watchee(
            plainId = "plainId$it",
            title = "title:$it",
            lastFetchedPrice = 0f,
            lastFetchedStoreName = "store",
            dateAdded = 0,
            targetPrice = 0f,
            regionCode = "EU1",
            countryCode = "DE",
            currencyCode = "EUR"
        )
    }
}