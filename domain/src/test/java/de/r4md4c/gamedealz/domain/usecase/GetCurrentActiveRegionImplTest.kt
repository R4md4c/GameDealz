package de.r4md4c.gamedealz.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.commonproviders.configuration.ConfigurationProvider
import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class GetCurrentActiveRegionImplTest {

    @Mock
    private lateinit var getRegionsUseCase: GetRegionsUseCase

    @Mock
    private lateinit var configurationProvider: ConfigurationProvider

    private lateinit var subject: GetCurrentActiveRegionImpl

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        subject = GetCurrentActiveRegionImpl(getRegionsUseCase, configurationProvider)
    }


    @Test
    fun `it should return the active region from the configuration locale`() {
        val mockRegion = mock<Region>()
        val mockCurrency = mock<Currency>()
        ArrangeBuilder()
            .withLocale(Locale("aLanguage", "aCountry"))
            .withRegionsWithCountries(
                listOf(
                    RegionWithCountries(
                        mockRegion, mockCurrency,
                        setOf(Country("aCountry", ""))
                    )
                )
            )

        val result = runBlocking {
            subject()
        }

        assertThat(result).isEqualTo(ActiveRegion(mockRegion, Country("aCountry", ""), mockCurrency))
    }

    @Test
    fun `it should return the default region when locale is not among the regions`() {
        val mockCurrency = mock<Currency>()
        ArrangeBuilder()
            .withLocale(Locale("aLanguage", "aCountry"))
            .withRegionsWithCountries(
                listOf(
                    RegionWithCountries(
                        Region(DEFAULT_REGION, ""),
                        mockCurrency,
                        setOf(Country(DEFAULT_COUNTRY, ""))
                    )
                )
            )

        val result = runBlocking {
            subject()
        }

        assertThat(result).isEqualTo(
            ActiveRegion(
                Region(DEFAULT_REGION, ""),
                Country(DEFAULT_COUNTRY, ""), mockCurrency
            )
        )
    }

    inner class ArrangeBuilder {

        fun withRegionsWithCountries(regionsWithCountries: List<RegionWithCountries>) = apply {
            runBlocking { whenever(getRegionsUseCase.regions()).thenReturn(regionsWithCountries) }
        }

        fun withLocale(locale: Locale) = apply {
            whenever(configurationProvider.locale).thenReturn(locale)
        }
    }

    private companion object {
        private const val DEFAULT_REGION = "us"
        private const val DEFAULT_COUNTRY = "US"
    }
}