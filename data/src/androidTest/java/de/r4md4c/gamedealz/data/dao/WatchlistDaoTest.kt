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
import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.r4md4c.gamedealz.data.GameDealzDatabase
import de.r4md4c.gamedealz.data.entity.Watchee
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WatchlistDaoTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var watchlistDao: WatchlistDao

    private lateinit var database: GameDealzDatabase

    private val context
        get() = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun beforeEach() {
        database = Room.inMemoryDatabaseBuilder(context, GameDealzDatabase::class.java)
            .build()
        watchlistDao = database.watchlistDao()
    }

    @After
    fun afterEach() {
        database.close()
    }

    @Test
    fun insert() = runBlockingTest {
        watchlistDao.insert(watcheesList)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertAbortsTransactionWhenDuplicateErrors() = runBlockingTest {
        watchlistDao.insert(watcheesList)

        watchlistDao.insert(listOf(watcheesList[1]))
    }

    @Test
    fun findOne_findsReallyOne() = runBlockingTest {
        watchlistDao.insert(watcheesList)

        assertThat(watchlistDao.findOne("plainId5").first()).isNotEmpty()
    }

    @Test
    fun update() = runBlockingTest {
        watchlistDao.insert(watcheesList)

        val watchee = watchlistDao.findOne("plainId5").first().first()

        val timeStamp = System.currentTimeMillis()
        assertThat(
            watchlistDao.updateWatchee(
                watchee.id,
                500f,
                "store",
                timeStamp
            )
        ).isEqualTo(1)
        assertThat(watchlistDao.findOne("plainId5").first().first()).isEqualTo(
            watchee.copy(
                lastFetchedPrice = 500f,
                lastCheckDate = timeStamp
            )
        )
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
    fun mostRecentLastCheckDate() = runBlockingTest {
        watchlistDao.insert(watcheesList.mapIndexed { index: Int, watchee: Watchee ->
            watchee.copy(
                lastCheckDate = index.toLong() + 1
            )
        })

        assertThat(watchlistDao.mostRecentLastCheckDate().first()).isEqualTo(10)
    }

    @Test
    fun mostRecentLastCheckDate_whenEmpty() = runBlockingTest {
        assertThat(watchlistDao.mostRecentLastCheckDate().first()).isEqualTo(0)
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