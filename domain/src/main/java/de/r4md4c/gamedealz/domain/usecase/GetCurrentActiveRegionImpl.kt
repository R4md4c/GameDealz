package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.commonproviders.configuration.ConfigurationProvider
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.findCountry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCurrentActiveRegionImpl(
    private val getRegionsUseCase: GetRegionsUseCase,
    private val configurationProvider: ConfigurationProvider
) : GetCurrentActiveRegion {

    override suspend fun invoke(): ActiveRegion {
        val regions = withContext(Dispatchers.IO) { getRegionsUseCase.regions() }
        val locale = configurationProvider.locale

        return withContext(Dispatchers.Default) {
            // We are doing an n^2 search, doesn't matter since it is a constant length array.
            val localeBasedRegionWithCountries = regions.asSequence().firstOrNull {
                it.findCountry(locale.country) != null
            }

            localeBasedRegionWithCountries?.let {
                ActiveRegion(it.region, it.findCountry(locale.country)!!, it.currency)
            } ?: getDefaultRegionAndCountry(regions)
        }
    }

    private fun getDefaultRegionAndCountry(regions: List<RegionWithCountries>): ActiveRegion {
        val defaultRegion = regions.asSequence().first { it.region.regionCode == DEFAULT_REGION }
        val defaultCountry = defaultRegion.countries.asSequence().first { it.code.equals(DEFAULT_COUNTRY, true) }

        return ActiveRegion(defaultRegion.region, defaultCountry, defaultRegion.currency)
    }

    private companion object {
        private const val DEFAULT_REGION = "us"
        private const val DEFAULT_COUNTRY = "US"
    }
}