package de.r4md4c.gamedealz.domain.model

import com.google.common.truth.Truth.assertThat
import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.RegionWithCountries

import org.junit.Test

class EntitiesExtTest {

    @Test
    fun `findCountry when country exists`() {
        val aRegion = RegionWithCountries(Region("", ""), Currency("", ""), countries())

        assertThat(aRegion.findCountry("code3")).isNotNull()
    }

    @Test
    fun `findCountry when country does not exist`() {
        val aRegion = RegionWithCountries(Region("", ""), Currency("", ""), countries())

        assertThat(aRegion.findCountry("code100")).isNull()
    }

    @Test
    fun displayName() {
        val country = Country("US", "us")

        assertThat(country.displayName()).isEqualTo("United States")
    }

    private fun countries() = (1..10).map { Country("code$it", "") }.toSet()
}