package de.r4md4c.gamedealz.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import de.r4md4c.gamedealz.data.DATA
import de.r4md4c.gamedealz.data.Fixtures
import de.r4md4c.gamedealz.data.GameDealzDatabase
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

class PlainsDaoTest : KoinTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val plainsDao: PlainsDao by inject()

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