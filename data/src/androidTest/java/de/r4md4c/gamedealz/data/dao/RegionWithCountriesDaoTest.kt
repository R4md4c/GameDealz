package de.r4md4c.gamedealz.data.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.r4md4c.gamedealz.data.DATA
import de.r4md4c.gamedealz.data.Fixtures
import de.r4md4c.gamedealz.data.GameDealzDatabase
import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.Region
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
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

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val roomWithCountriesDao: RegionWithCountriesDao by inject()

    @Before
    fun beforeEach() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        startKoin(listOf(DATA, module {
            single(override = true) {
                Room.inMemoryDatabaseBuilder(androidContext(), GameDealzDatabase::class.java)
                    .build()
            }
        })).with(context)
    }

    @After
    fun afterEach() {
        get<GameDealzDatabase>().close()
        stopKoin()
    }

    @Test
    fun allRegions_ReturnEmpty() = runBlockingTest {
        val regions = roomWithCountriesDao.allRegions()

        assertThat(regions.first()).isEmpty()
    }

    @Test
    fun allRegions_whenNoCountriesStored() = runBlockingTest {
        ArrangeBuilder()
            .withCurrency(currencyList())
            .withRegions((1..10).map {
                Fixtures.region(
                    "region$it",
                    currencyList().first().currencyCode
                )
            })
            .withCountries(emptyList())
            .arrange()

        val regions = roomWithCountriesDao.allRegions().first()

        assertThat(regions).hasSize(10)
    }

    @Test
    fun allRegions_withCurrencies() = runBlockingTest {
        ArrangeBuilder()
            .withCurrency(currencyList())
            .withRegions((1..10).map {
                Fixtures.region(
                    "region$it",
                    currencyList().first().currencyCode
                )
            })
            .withCountries(emptyList())
            .arrange()

        val regions = roomWithCountriesDao.allRegions()


        regions.map { regionsWithCountries ->
            assertThat(regionsWithCountries.map { it.currency }).contains(currencyList().first())
        }

    }

    @Test
    fun allRegions_whenCountriesAreStored() = runBlockingTest {
        ArrangeBuilder()
            .withCurrency(currencyList())
            .withRegions((1..10).map {
                Fixtures.region(
                    "region$it",
                    currencyList().first().currencyCode
                )
            })
            .withCountries((1..10).map {
                Fixtures.country(
                    "country$it",
                    Fixtures.region("region$it", Fixtures.currency().currencyCode)
                )
            })
            .arrange()

        val regions = roomWithCountriesDao.allRegions()

        assertThat(regions.first()).hasSize(10)
    }

    @Test
    fun allRegions_whenMultipleCountriesAreStoredPerRegion() = runBlockingTest {
        ArrangeBuilder()
            .withCurrency(currencyList())
            .withRegions(listOf(Fixtures.region("region", currencyList().first().currencyCode)))
            .withCountries((1..10).map {
                Fixtures.country(
                    "country$it",
                    Fixtures.region("region", Fixtures.currency().currencyCode)
                )
            })
            .arrange()

        val regions = roomWithCountriesDao.allRegions()

        val firstRegion = regions.first()
        assertThat(firstRegion).hasSize(1)
        assertThat(firstRegion.first().countries).hasSize(10)
    }

    @Test
    fun region_whenExists() = runBlockingTest {
        ArrangeBuilder()
            .withCurrency(currencyList())
            .withRegions(listOf(Fixtures.region("region", currencyList().first().currencyCode)))
            .withCountries((1..10).map {
                Fixtures.country(
                    "country$it",
                    Fixtures.region("region", currencyList().first().currencyCode)
                )
            })
            .arrange()

        val region = roomWithCountriesDao.region("region")

        assertThat(region).isNotNull()
        assertThat(region?.countries).hasSize(10)
    }

    @Test
    fun region_whenNotExist() = runBlockingTest {
        ArrangeBuilder()
            .withCurrency(emptyList())
            .withRegions(emptyList())
            .withCountries(emptyList())
            .arrange()

        val region = roomWithCountriesDao.region("region")

        assertThat(region).isNull()

    }

    @Test(expected = SQLiteConstraintException::class)
    fun relationshipBetweenCountriesAndRegion_whenStoringWithNonExistingRegion() = runBlockingTest {
        ArrangeBuilder()
            .withCurrency(currencyList())
            .withRegions(listOf(Region("region", currencyList().first().currencyCode)))
            .withCountries((1..10).map { Country("country$it", "non-existingregion") })
            .arrange()

        roomWithCountriesDao.allRegions()
    }

    private fun currencyList() = (1..3).map { Fixtures.currency("currency$it") }

    private inner class ArrangeBuilder {
        private var regions: List<Region> by Delegates.notNull()

        private var countries: List<Country> by Delegates.notNull()

        private var currencies: List<Currency> by Delegates.notNull()

        fun withRegions(regions: List<Region>) = apply {
            this.regions = regions
        }

        fun withCountries(countries: List<Country>) = apply {
            this.countries = countries
        }

        fun withCurrency(currencies: List<Currency>) = apply {
            this.currencies = currencies
        }

        fun arrange() = apply {
            runBlocking {
                roomWithCountriesDao.insertRegionsWithCountries(currencies, regions, countries)
            }
        }
    }
}
