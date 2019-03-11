package de.r4md4c.gamedealz.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EntitiesExtTest {

    @Test
    fun `findCountry when country exists`() {
        val aRegion = RegionWithCountriesModel("", CurrencyModel("", ""), countries())

        assertThat(aRegion.findCountry("code3")).isNotNull()
    }

    @Test
    fun `findCountry when country does not exist`() {
        val aRegion = RegionWithCountriesModel("", CurrencyModel("", ""), countries())

        assertThat(aRegion.findCountry("code100")).isNull()
    }

    @Test
    fun displayName() {
        val country = CountryModel("US")

        assertThat(country.displayName()).isEqualTo("United States")
    }

    private fun countries() = (1..10).map { CountryModel("code$it") }
}