package de.r4md4c.gamedealz.data.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.r4md4c.gamedealz.data.Fixtures
import de.r4md4c.gamedealz.data.GameDealzDatabase
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PlainsDaoTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var plainsDao: PlainsDao

    private lateinit var database: GameDealzDatabase

    private val context
        get() = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun beforeEach() {
        database = Room.inMemoryDatabaseBuilder(context, GameDealzDatabase::class.java).build()
        plainsDao = database.plainsDao()
    }

    @After
    fun afterEach() {
        database.close()
    }

    @Test
    fun count_returnsZeroWhenTableEmpty() {
        val result = runBlocking {
            plainsDao.count()
        }

        assertThat(result).isEqualTo(0)
    }

    @Test
    fun count_returnsCorrectCount() {
        runBlocking {
            (1..20).map { Fixtures.plain().copy(id = "id$it") }.run {
                plainsDao.insert(this)
            }

            assertThat(plainsDao.count()).isEqualTo(20)
        }
    }

    @Test
    fun findOne_returnsFoundItem() {
        runBlocking {
            (1..20).map { Fixtures.plain().copy(id = "id$it") }.run {
                plainsDao.insert(this)
            }

            assertThat(plainsDao.findOne("id5")).isNotNull()
        }
    }
}