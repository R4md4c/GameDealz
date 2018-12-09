package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import de.r4md4c.gamedealz.data.repository.RegionsRepository as LocalRegionRepository
import de.r4md4c.gamedealz.network.repository.RegionsRepository as RemoteRegionRepository

internal class GetStoredRegionsUseCase(
    private val localRepository: LocalRegionRepository,
    private val remoteRepository: RemoteRegionRepository
) : GetRegionsUseCase {

    override suspend fun regions(): List<RegionWithCountries> =
        withContext(IO) {
            val localRegions = localRepository.all()
            if (!localRegions.isEmpty()) {
                localRegions
            } else {
                val regionsWithCountries = loadRegionsFromServer()
                localRepository.save(regionsWithCountries)

                localRepository.all()
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
}