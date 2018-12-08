package de.r4md4c.gamedealz.data.repository

import com.nhaarman.mockitokotlin2.verify
import de.r4md4c.gamedealz.data.dao.RegionWithCountriesDao
import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class RegionLocalRepositoryTest {

    private lateinit var regionLocalRepository: RegionLocalRepository

    @Mock
    private lateinit var regionsWithCountriesDao: RegionWithCountriesDao

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        regionLocalRepository = RegionLocalRepository(regionsWithCountriesDao)
    }

    @Test
    fun allRegions() {
        runBlocking {
            regionLocalRepository.all()

            verify(regionsWithCountriesDao).allRegions()
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
            val regions = (1..10).map { Region("Region$it") }
            val countries = (1..10).map { Country("Code$it", "Region$it") }
            val regionWithCountries = regions.zip(countries).map { RegionWithCountries(it.first, setOf(it.second)) }

            regionLocalRepository.save(regionWithCountries)

            verify(regionsWithCountriesDao).insertRegionsWithCountries(regions, countries)
        }
    }
}