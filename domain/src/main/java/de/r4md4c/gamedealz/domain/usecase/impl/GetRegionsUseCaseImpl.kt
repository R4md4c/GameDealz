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

import de.r4md4c.commonproviders.coroutines.GameDealzDispatchers.IO
import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.Plain
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import de.r4md4c.gamedealz.data.repository.PlainsRepository
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.RegionWithCountriesModel
import de.r4md4c.gamedealz.domain.model.toModel
import de.r4md4c.gamedealz.domain.usecase.GetRegionsUseCase
import de.r4md4c.gamedealz.network.repository.PlainsRemoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import de.r4md4c.gamedealz.data.repository.RegionsRepository as LocalRegionRepository
import de.r4md4c.gamedealz.network.repository.RegionsRemoteRepository as RemoteRegionRepository

internal class GetRegionsUseCaseImpl(
    private val localRepository: LocalRegionRepository,
    private val remoteRepository: RemoteRegionRepository,
    private val plainsLocalRepository: PlainsRepository,
    private val plainsRemoteRepository: PlainsRemoteRepository
) : GetRegionsUseCase {

    override suspend fun invoke(param: VoidParameter?): List<RegionWithCountriesModel> =
        withContext(IO) {
            retrieveAndStorePlainsFromSteam()

            val localRegions = localRepository.all().first()

            if (localRegions.isNotEmpty()) {
                localRegions.map { it.toModel() }
            } else {
                val regionsWithCountries = loadRegionsFromServer()
                localRepository.save(regionsWithCountries)

                localRepository.all().first().map { it.toModel() }
            }
        }

    private suspend fun loadRegionsFromServer(): List<RegionWithCountries> {
        val serverRegions = remoteRepository.regions()

        return serverRegions.map {
            val region = Region(it.key, it.value.currency.code)
            val countries =
                it.value.countries.map { countryCode -> Country(countryCode, region.regionCode) }.toSet()
            RegionWithCountries(region, Currency(it.value.currency.code, it.value.currency.sign), countries)
        }
    }

    private suspend fun retrieveAndStorePlainsFromSteam() {
        // TODO: Handle updating plains daily.
        val count = plainsLocalRepository.count()
        if (count > 0) return

        val plainsList = plainsRemoteRepository.plainsList(setOf(STEAM_SHOP_ID))
        plainsList[STEAM_SHOP_ID]?.map { Plain(it.value, it.key) }?.run {
            plainsLocalRepository.save(this)
        }
    }

    companion object {
        private const val STEAM_SHOP_ID = "steam"
    }
}
