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

package de.r4md4c.gamedealz.regions

import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.r4md4c.commonproviders.coroutines.GameDealzDispatchers.IO
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.CountryModel
import de.r4md4c.gamedealz.domain.model.displayName
import de.r4md4c.gamedealz.domain.usecase.ChangeActiveRegionParameter
import de.r4md4c.gamedealz.domain.usecase.ChangeActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCountriesUnderRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetRegionsUseCase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

data class RegionSelectionModel(val regions: List<String>, val activeRegionIndex: Int)
data class CountrySelectionModel(val countries: List<String>, val activeCountryIndex: Int?)

class RegionSelectionViewModel(
    private val countriesUnderRegionUseCase: GetCountriesUnderRegionUseCase,
    private val getRegionsUseCase: GetRegionsUseCase,
    private val changeActiveRegionUseCase: ChangeActiveRegionUseCase
) : AbstractViewModel() {

    private val displayNameToCountryCodeMap: MutableMap<String, CountryModel> by lazy { ArrayMap<String, CountryModel>() }

    private val _regions by lazy { MutableLiveData<RegionSelectionModel>() }
    val regions: LiveData<RegionSelectionModel> by lazy { _regions }

    private val _countries by lazy { MutableLiveData<CountrySelectionModel>() }
    val countries: LiveData<CountrySelectionModel> by lazy { _countries }

    fun requestRegions(activeRegion: ActiveRegion, restoreRegionIndex: Int?) {
        uiScope.launch(IO) {

            //Filter out regions that have no countries
            val allRegions = getRegionsUseCase().filter { it.countries.isNotEmpty() }

            _regions.postValue(
                RegionSelectionModel(
                    allRegions.map { it.regionCode.toUpperCase() },
                    restoreRegionIndex ?: allRegions.indexOfFirst { r -> r.regionCode == activeRegion.regionCode })
            )

        }
    }

    fun requestCountriesUnderRegion(activeRegion: ActiveRegion, restoreCountryIndex: Int?) {
        loadCountries(activeRegion.regionCode.toLowerCase()) {
            _countries.postValue(
                CountrySelectionModel(it.map { model -> model.displayName() },
                    restoreCountryIndex ?: it.indexOfFirst { c -> c.code == activeRegion.country.code })
            )
        }
    }

    fun onRegionSelected(regionCode: String) {
        loadCountries(regionCode.toLowerCase()) {
            _countries.postValue(CountrySelectionModel(it.map { model -> model.displayName() }, 0))
        }
    }

    fun onSubmitResult(regionCode: String, countryDisplayName: String) {
        val countryModel = displayNameToCountryCodeMap[countryDisplayName] ?: return

        // Launching to the global scope so that we don't get tied to this VM's lifecycle.
        GlobalScope.launch {
            changeActiveRegionUseCase(
                TypeParameter(
                    ChangeActiveRegionParameter(
                        regionCode.toLowerCase(),
                        countryModel.code
                    )
                )
            )
        }
    }

    private inline fun loadCountries(regionCode: String, crossinline block: (List<CountryModel>) -> Unit) {
        uiScope.launch(IO) {
            val countries = countriesUnderRegionUseCase(TypeParameter(regionCode))
            saveDisplayNames(countries)
            block(countries)
        }
    }

    private fun saveDisplayNames(countryModels: List<CountryModel>) {
        displayNameToCountryCodeMap.clear()
        countryModels.associateByTo(displayNameToCountryCodeMap) { countryModel ->
            countryModel.displayName()
        }
    }

}