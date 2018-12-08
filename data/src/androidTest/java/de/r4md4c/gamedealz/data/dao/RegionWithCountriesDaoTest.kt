package de.r4md4c.gamedealz.data.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import de.r4md4c.gamedealz.data.DATA
import de.r4md4c.gamedealz.data.Fixtures
import de.r4md4c.gamedealz.data.GameDealzDatabase
import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Region
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.with
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.get
import org.koin.standalone.inject
import org.koin.test.KoinTest
import kotlin.properties.Delegates

@RunWith(AndroidJUnit4::class)
class RegionWithCountriesDaoTest : KoinTest {

    private val roomWithCountriesDao: RegionWithCountriesDao by inject()

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
        get<GameDealzDatabase>().close()
        stopKoin()
    }

    @Test
    fun allRegions_ReturnEmpty() {
        runBlocking {
            val regions = roomWithCountriesDao.allRegions()

            assertThat(regions).isEmpty()
        }
    }

    @Test
    fun allRegions_whenNoCountriesStored() {
        runBlocking {
            ArrangeBuilder()
                .withRegions((1..10).map { Fixtures.region("region$it") })
                .withCountries(emptyList())
                .arrange()

            val regions = roomWithCountriesDao.allRegions()

            assertThat(regions).hasSize(10)
        }
    }

    @Test
    fun allRegions_whenCountriesAreStored() {
        runBlocking {
            ArrangeBuilder()
                .withRegions((1..10).map { Fixtures.region("region$it") })
                .withCountries((1..10).map { Fixtures.country("country$it", Fixtures.region("region$it")) })
                .arrange()

            val regions = roomWithCountriesDao.allRegions()

            assertThat(regions).hasSize(10)
            regions.forEach { assertThat(it.countries).hasSize(1) }
        }
    }

    @Test
    fun allRegions_whenMultipleCountriesAreStoredPerRegion() {
        runBlocking {
            ArrangeBuilder()
                .withRegions(listOf(Fixtures.region("region")))
                .withCountries((1..10).map { Fixtures.country("country$it", Fixtures.region("region")) })
                .arrange()

            val regions = roomWithCountriesDao.allRegions()

            assertThat(regions).hasSize(1)
            assertThat(regions.first().countries).hasSize(10)
        }
    }

    @Test
    fun region_whenExists() {
        runBlocking {
            ArrangeBuilder()
                .withRegions(listOf(Fixtures.region("region")))
                .withCountries((1..10).map { Fixtures.country("country$it", Fixtures.region("region")) })
                .arrange()

            val region = roomWithCountriesDao.region("region")

            assertThat(region).isNotNull()
            assertThat(region?.countries).hasSize(10)
        }
    }

    @Test
    fun region_whenNotExist() {
        runBlocking {
            ArrangeBuilder()
                .withRegions(emptyList())
                .withCountries(emptyList())
                .arrange()

            val region = roomWithCountriesDao.region("region")

            assertThat(region).isNull()
        }
    }

    @Test(expected = SQLiteConstraintException::class)
    fun relationshipBetweenCountriesAndRegion_whenStoringWithNonExistingRegion() {
        runBlocking {
            ArrangeBuilder()
                .withRegions(listOf(Region("region")))
                .withCountries((1..10).map { Country("country$it", "non-existingregion") })
                .arrange()

            roomWithCountriesDao.allRegions()
        }
    }

    private inner class ArrangeBuilder {
        private var regions: List<Region> by Delegates.notNull()

        private var countries: List<Country> by Delegates.notNull()

        fun withRegions(regions: List<Region>) = apply {
            this.regions = regions
        }

        fun withCountries(countries: List<Country>) = apply {
            this.countries = countries
        }

        fun arrange() = apply {
            runBlocking {
                roomWithCountriesDao.insertRegionsWithCountries(regions, countries)
            }
        }
    }
}
