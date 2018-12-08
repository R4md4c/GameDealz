package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Region
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import de.r4md4c.gamedealz.data.repository.RegionsRepository as LocalRegionRepository
import de.r4md4c.gamedealz.network.repository.RegionsRepository as RemoteRegionRepository

internal class GetStoredRegionsUseCase(
    private val localRepository: LocalRegionRepository,
    private val remoteRepository: RemoteRegionRepository
) : GetRegionsUseCase {

    override suspend fun regions(): List<RegionWithCountries> = coroutineScope {
        withContext(IO) {
            val serverRegions = remoteRepository.regions()

            val regionsWithCountries = serverRegions.map {
                val region = Region(it.key)
                val countries = it.value.countries.map { countryCode -> Country(countryCode, region.code) }.toSet()
                RegionWithCountries(region, countries)
            }

            localRepository.save(regionsWithCountries)
            localRepository.all()
        }
    }

}