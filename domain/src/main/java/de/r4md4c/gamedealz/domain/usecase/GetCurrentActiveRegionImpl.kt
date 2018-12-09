package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.commonproviders.configuration.ConfigurationProvider
import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.findCountry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class GetCurrentActiveRegionImpl(
    private val getRegionsUseCase: GetRegionsUseCase,
    private val configurationProvider: ConfigurationProvider,
    private val sharedPreferences: SharedPreferencesProvider
) : GetCurrentActiveRegion {

    override suspend fun invoke(): ActiveRegion {
        val savedRegionCountryPair = sharedPreferences.activeRegionAndCountry
        val regions = withContext(Dispatchers.IO) { getRegionsUseCase.regions() }

        return withContext(Dispatchers.Default) {
            if (savedRegionCountryPair == null) {
                val locale = configurationProvider.locale
                val localeBasedRegionWithCountries = regions.findRegionAndCountryByLocale(locale)
                localeBasedRegionWithCountries?.let {
                    ActiveRegion(it.region, it.findCountry(locale.country)!!, it.currency)
                } ?: regions.getRegionAndCountry(DEFAULT_REGION, DEFAULT_COUNTRY)!!
            } else {
                regions.getRegionAndCountry(savedRegionCountryPair.first, savedRegionCountryPair.second)!!
            }.apply {
                sharedPreferences.activeRegionAndCountry = region.regionCode to country.code
            }
        }
    }

    private fun List<RegionWithCountries>.findRegionAndCountryByLocale(locale: Locale): RegionWithCountries? =
        asSequence().firstOrNull {
            it.findCountry(locale.country) != null
        }

    private fun List<RegionWithCountries>.getRegionAndCountry(regionCode: String, countryCode: String): ActiveRegion? {
        val foundRegion = asSequence().first { it.region.regionCode == regionCode }
        val foundCountry = foundRegion.countries.asSequence().first { it.code.equals(countryCode, true) }

        return ActiveRegion(foundRegion.region, foundCountry, foundRegion.currency)
    }

    private companion object {
        private const val DEFAULT_REGION = "us"
        private const val DEFAULT_COUNTRY = "US"
    }
}