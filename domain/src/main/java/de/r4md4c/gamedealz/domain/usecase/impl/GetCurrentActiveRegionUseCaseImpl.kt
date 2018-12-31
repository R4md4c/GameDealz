/*
 * This file is part of GameDealz.
 *
 * GameDealz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * GameDealz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GameDealz.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.commonproviders.configuration.ConfigurationProvider
import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.RegionWithCountriesModel
import de.r4md4c.gamedealz.domain.model.findCountry
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetRegionsUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.mapNotNull
import kotlinx.coroutines.withContext
import java.util.*

internal class GetCurrentActiveRegionUseCaseImpl(
    private val getRegionsUseCase: GetRegionsUseCase,
    private val configurationProvider: ConfigurationProvider,
    private val sharedPreferences: SharedPreferencesProvider
) : GetCurrentActiveRegionUseCase, OnCurrentActiveRegionReactiveUseCase {


    override suspend fun invoke(param: VoidParameter?): ActiveRegion {
        val savedRegionCountryPair = sharedPreferences.activeRegionAndCountry
        val regions = withContext(Dispatchers.IO) { getRegionsUseCase() }

        return withContext(Dispatchers.Default) {
            if (savedRegionCountryPair == null) {
                val locale = configurationProvider.locale
                val localeBasedRegionWithCountries = regions.findRegionAndCountryByLocale(locale)
                val result = localeBasedRegionWithCountries?.let {
                    ActiveRegion(
                        it.regionCode,
                        it.findCountry(locale.country)!!,
                        it.currency
                    )
                } ?: regions.getRegionAndCountry(DEFAULT_REGION, DEFAULT_COUNTRY)!!
                sharedPreferences.activeRegionAndCountry = result.regionCode to result.country.code
                result
            } else {
                regions.getRegionAndCountry(savedRegionCountryPair.first, savedRegionCountryPair.second)!!
            }
        }
    }

    override suspend fun activeRegionChange(): ReceiveChannel<ActiveRegion> =
        sharedPreferences.activeRegionAndCountryChannel
            .mapNotNull {
                val regions = withContext(Dispatchers.IO) { getRegionsUseCase() }
                regions.getRegionAndCountry(it.first, it.second)
            }

    private fun List<RegionWithCountriesModel>.findRegionAndCountryByLocale(locale: Locale): RegionWithCountriesModel? =
        asSequence().firstOrNull {
            it.findCountry(locale.country) != null
        }

    private fun List<RegionWithCountriesModel>.getRegionAndCountry(
        regionCode: String,
        countryCode: String
    ): ActiveRegion? {
        val foundRegion = asSequence().first { it.regionCode == regionCode }
        val foundCountry = foundRegion.countries.asSequence().first { it.code.equals(countryCode, true) }

        return ActiveRegion(
            foundRegion.regionCode,
            foundCountry,
            foundRegion.currency
        )
    }

    private companion object {
        private const val DEFAULT_REGION = "us"
        private const val DEFAULT_COUNTRY = "US"
    }
}