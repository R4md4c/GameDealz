package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.commonproviders.configuration.ConfigurationProvider
import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.findCountry
import de.r4md4c.gamedealz.domain.model.toCountryModel
import de.r4md4c.gamedealz.domain.model.toCurrencyModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetRegionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

internal class GetCurrentActiveRegionUseCaseImpl(
    private val getRegionsUseCase: GetRegionsUseCase,
    private val configurationProvider: ConfigurationProvider,
    private val sharedPreferences: SharedPreferencesProvider
) : GetCurrentActiveRegionUseCase {


    override suspend fun invoke(param: VoidParameter?): ActiveRegion {
        val savedRegionCountryPair = sharedPreferences.activeRegionAndCountry
        val regions = withContext(Dispatchers.IO) { getRegionsUseCase() }

        return withContext(Dispatchers.Default) {
            if (savedRegionCountryPair == null) {
                val locale = configurationProvider.locale
                val localeBasedRegionWithCountries = regions.findRegionAndCountryByLocale(locale)
                localeBasedRegionWithCountries?.let {
                    ActiveRegion(
                        it.region.regionCode,
                        it.findCountry(locale.country)!!.toCountryModel(),
                        it.currency.toCurrencyModel()
                    )
                } ?: regions.getRegionAndCountry(DEFAULT_REGION, DEFAULT_COUNTRY)!!
            } else {
                regions.getRegionAndCountry(savedRegionCountryPair.first, savedRegionCountryPair.second)!!
            }.apply {
                sharedPreferences.activeRegionAndCountry = regionCode to country.code
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

        return ActiveRegion(
            foundRegion.region.regionCode,
            foundCountry.toCountryModel(),
            foundRegion.currency.toCurrencyModel()
        )
    }

    private companion object {
        private const val DEFAULT_REGION = "us"
        private const val DEFAULT_COUNTRY = "US"
    }
}