package de.r4md4c.gamedealz.domain.usecase.impl

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import de.r4md4c.commonproviders.configuration.ConfigurationProvider
import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.usecase.GetRegionsUseCase
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

    @Mock
    private lateinit var sharedPreferencesProvider: SharedPreferencesProvider

    private lateinit var subject: GetCurrentActiveRegionUseCaseImpl

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        subject = GetCurrentActiveRegionUseCaseImpl(getRegionsUseCase, configurationProvider, sharedPreferencesProvider)
    }


    @Test
    fun `it should return the active region from the configuration locale`() {
        val mockRegion = mock<Region>()
        val mockCurrency = mock<Currency>()
        ArrangeBuilder()
            .withNullSharedPreferenceValue()
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

        verify(configurationProvider).locale
        assertThat(result).isEqualTo(ActiveRegion(mockRegion, Country("aCountry", ""), mockCurrency))
    }

    @Test
    fun `it should return the default region when locale is not among the regions`() {
        val mockCurrency = mock<Currency>()
        ArrangeBuilder()
            .withNullSharedPreferenceValue()
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

    @Test
    fun `it should try to return result from shared preferences first`() {
        val mockCurrency = mock<Currency>()
        ArrangeBuilder()
            .withSharedPreference(
                DEFAULT_REGION,
                DEFAULT_COUNTRY
            )
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
        verify(sharedPreferencesProvider).activeRegionAndCountry
        verify(configurationProvider, never()).locale
    }

    @Test
    fun `it should save to shared preferences at the end`() {
        val mockCurrency = mock<Currency>()
        ArrangeBuilder()
            .withNullSharedPreferenceValue()
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

        runBlocking {
            subject()
        }

        verify(sharedPreferencesProvider).activeRegionAndCountry = DEFAULT_REGION to
                DEFAULT_COUNTRY
    }

    inner class ArrangeBuilder {

        fun withRegionsWithCountries(regionsWithCountries: List<RegionWithCountries>) = apply {
            runBlocking { whenever(getRegionsUseCase.invoke(anyOrNull())).thenReturn(regionsWithCountries) }
        }

        fun withLocale(locale: Locale) = apply {
            whenever(configurationProvider.locale).thenReturn(locale)
        }

        fun withSharedPreference(regionCode: String, countryCode: String) = apply {
            whenever(sharedPreferencesProvider.activeRegionAndCountry).thenReturn(regionCode to countryCode)
        }

        fun withNullSharedPreferenceValue() = apply {
            whenever(sharedPreferencesProvider.activeRegionAndCountry).thenReturn(null)
        }
    }

    private companion object {
        private const val DEFAULT_REGION = "us"
        private const val DEFAULT_COUNTRY = "US"
    }
}