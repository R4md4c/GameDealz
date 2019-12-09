package de.r4md4c.gamedealz.data.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.r4md4c.gamedealz.data.GameDealzDatabase
import de.r4md4c.gamedealz.data.entity.Store
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.properties.Delegates

@RunWith(AndroidJUnit4::class)
class StoresDaoTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var storesDao: StoresDao

    private lateinit var database: GameDealzDatabase

    private val context
        get() = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun beforeEach() {
        database = Room.inMemoryDatabaseBuilder(context, GameDealzDatabase::class.java)
            .build()
        storesDao = database.storesDao()
    }

    @After
    fun afterEach() {
        database.close()
    }

    @Test
    fun all_retrievesAllStores() = runBlockingTest {
        ArrangeBuilder()
            .arrange()

        val allStores = storesDao.all().first()

        assertThat(allStores).hasSize(10)
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

    @Test
    fun updateSelected() {
        val storesList = this.storesList
        ArrangeBuilder()
            .withStores(storesList)
            .arrange()

        val result = storesDao.updateSelected(true, storesList.map { it.id }.toSet())

        assertThat(result).isEqualTo(10)
    }

    @Test
    fun allSelected() = runBlockingTest {
        val storesList = storesList.take(5).map { it.copy(selected = true) }
        ArrangeBuilder()
            .withStores(storesList)
            .arrange()

        val result = storesDao.allSelected().first()

        assertThat(result).hasSize(5)
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