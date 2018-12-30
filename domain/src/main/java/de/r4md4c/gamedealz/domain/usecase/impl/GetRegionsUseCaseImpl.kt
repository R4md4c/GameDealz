package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.gamedealz.data.entity.*
import de.r4md4c.gamedealz.data.repository.PlainsRepository
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.RegionWithCountriesModel
import de.r4md4c.gamedealz.domain.model.toModel
import de.r4md4c.gamedealz.domain.usecase.GetRegionsUseCase
import de.r4md4c.gamedealz.network.repository.PlainsRemoteRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.first
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

            if (!localRegions.isEmpty()) {
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
        //TODO: Handle updating plains daily.
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