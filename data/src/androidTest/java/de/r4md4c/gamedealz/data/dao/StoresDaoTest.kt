package de.r4md4c.gamedealz.data.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import de.r4md4c.gamedealz.data.DATA
import de.r4md4c.gamedealz.data.GameDealzDatabase
import de.r4md4c.gamedealz.data.entity.Store
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.with
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext
import org.koin.standalone.get
import org.koin.standalone.inject
import org.koin.test.KoinTest
import kotlin.properties.Delegates

@RunWith(AndroidJUnit4::class)
class StoresDaoTest : KoinTest {

    private val storesDao: StoresDao by inject()

    @Before
    fun beforeEach() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        StandAloneContext.startKoin(listOf(DATA, module {
            single(override = true) {
                Room.inMemoryDatabaseBuilder(androidContext(), GameDealzDatabase::class.java).build()
            }
        })).with(context)
    }

    @After
    fun afterEach() {
        get<GameDealzDatabase>().close()
        StandAloneContext.stopKoin()
    }

    @Test
    fun all_retrievesAllStores() {
        runBlocking {
            ArrangeBuilder()
                .arrange()

            val allStores = storesDao.all()

            assertThat(allStores).hasSize(10)
        }
    }

    @Test
    fun singleStore_retrievesAStore_whenFound() {
        runBlocking {
            ArrangeBuilder()
                .arrange()

            val singleStore = storesDao.singleStore("id1")

            assertThat(singleStore).isNotNull()
        }
    }

    @Test
    fun singleStore_returnsNull_whenNotFound() {
        runBlocking {
            ArrangeBuilder()
                .withStores(emptyList())
                .arrange()

            val singleStore = storesDao.singleStore("id1")

            assertThat(singleStore).isNull()
        }
    }

    private val storesList = (1..10).map { Store("id$it", "name$it", "color$it") }

    private inner class ArrangeBuilder {
        private var stores: List<Store> by Delegates.notNull()

        init {
            withStores(storesList)
        }

        fun withStores(stores: List<Store>) = apply {
            this.stores = stores
        }

        fun arrange() = apply {
            runBlocking {
                storesDao.insert(stores)
            }
        }
    }
}