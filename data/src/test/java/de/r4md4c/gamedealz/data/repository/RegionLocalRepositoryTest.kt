package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.dao.RegionWithCountriesDao
import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RegionLocalRepositoryTest {

    private lateinit var regionLocalRepository: RegionLocalDataSourceImpl

    @Mock
    private lateinit var regionsWithCountriesDao: RegionWithCountriesDao

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        regionLocalRepository = RegionLocalDataSourceImpl(regionsWithCountriesDao)
    }

    @Test
    fun `all invokes allRegions when supplied collection is null`() {
        runBlocking {
            ArrangeBuilder()
            regionLocalRepository.all()

            verify(regionsWithCountriesDao).allRegions()
        }
    }

    @Test
    fun `all invokes allRegions with collection when supplied collection is null`() {
        runBlocking {
            ArrangeBuilder()
            regionLocalRepository.all(emptyList())

            verify(regionsWithCountriesDao).allRegions(any())
        }
    }

    @Test
    fun singleRegion() {
        runBlocking {
            val param = "region"

            regionLocalRepository.findById(param)

            verify(regionsWithCountriesDao).region(param)
        }
    }

    @Test
    fun save() {
        runBlocking {
            val currency = (1..10).map { Currency("Currency$it", "sign$it") }
            val regions = (1..10).map { Region("Region$it", "Currency$it") }
            val countries = (1..10).map { Country("Code$it", "Region$it") }
            val regionWithCountries = regions.zip(countries).zip(currency).map {
                RegionWithCountries(it.first.first, it.second, setOf(it.first.second))
            }

            regionLocalRepository.save(regionWithCountries)

            verify(regionsWithCountriesDao).insertRegionsWithCountries(currency, regions, countries)
        }
    }


    private inner class ArrangeBuilder {
        init {
            whenever(regionsWithCountriesDao.allRegions()).thenReturn(flowOf(emptyList()))
            whenever(regionsWithCountriesDao.allRegions(any())).thenReturn(flowOf(emptyList()))
        }
    }
}