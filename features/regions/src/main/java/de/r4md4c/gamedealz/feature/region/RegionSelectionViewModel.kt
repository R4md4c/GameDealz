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

package de.r4md4c.gamedealz.feature.region

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.domain.model.displayName
import de.r4md4c.gamedealz.domain.usecase.ChangeActiveRegionParameter
import de.r4md4c.gamedealz.domain.usecase.ChangeActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCountriesUnderRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetRegionsUseCase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class RegionSelectionViewModel @AssistedInject constructor(
    private val dispatchers: IDispatchers,
    private val countriesUnderRegionUseCase: GetCountriesUnderRegionUseCase,
    private val getRegionsUseCase: GetRegionsUseCase,
    private val changeActiveRegionUseCase: ChangeActiveRegionUseCase,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val activeRegion =
        RegionSelectionDialogFragmentArgs.fromSavedStateHandle(savedStateHandle).region

    private val selectedCountryIndex = savedStateHandle.getLiveData("countryIndex", -1)
    private val selectedRegionIndex = savedStateHandle.getLiveData("regionIndex", -1)

    private val stateBuilder = combine(
        selectedRegionIndex.asFlow(),
        selectedCountryIndex.asFlow()
    ) { regionIndex, countryIndex ->
        val regions = getRegionsUseCase()

        val regionSelectionModel = RegionSelectionModel(
            regions = regions.map { it.regionCode.uppercase() },
            activeRegionIndex = if (regionIndex == -1) {
                regions.indexOfFirst { it.regionCode == activeRegion.regionCode }
            } else {
                regionIndex
            },
            selectedRegionCode = regions.getOrNull(regionIndex)?.regionCode
        )

        val countries = countriesUnderRegionUseCase.invoke(
            regions[regionSelectionModel.activeRegionIndex].regionCode
        )
        val countrySelectionModel = CountrySelectionModel(
            countryDisplayNames = countries.map { it.displayName() },
            activeCountryIndex = if (countryIndex == -1) {
                countries.indexOfFirst { it.code == activeRegion.country.code }
            } else {
                countryIndex
            },
            selectedCountryModel = countries.getOrNull(countryIndex)
        )
        RegionSelectionViewState(regionSelectionModel, countrySelectionModel)
    }

    val state by lazy {
        stateBuilder.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            RegionSelectionViewState(
                RegionSelectionModel(), CountrySelectionModel()
            )
        ).also {
            loadRegions()
        }
    }

    private fun loadRegions() {
        viewModelScope.launch {
            val regions = getRegionsUseCase()
            selectedRegionIndex.value =
                regions.indexOfFirst { it.regionCode == activeRegion.regionCode }

            val countries = countriesUnderRegionUseCase.invoke(activeRegion.regionCode)
            selectedCountryIndex.value =
                countries.indexOfFirst { it.code == activeRegion.country.code }
        }
    }

    fun onRegionSelected(index: Int) {
        selectedRegionIndex.value = index
        selectedCountryIndex.value = 0
    }

    fun onCountrySelected(index: Int) {
        selectedCountryIndex.value = index
    }

    fun submitResult() {
        val regionCode = state.value.regionSelectionModel.selectedRegionCode ?: return
        val countryCode = state.value.countrySelectionModel.selectedCountryModel ?: return

        GlobalScope.launch {
            changeActiveRegionUseCase.invoke(
                ChangeActiveRegionParameter(regionCode = regionCode, countryCode = countryCode.code)
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): RegionSelectionViewModel
    }
}
