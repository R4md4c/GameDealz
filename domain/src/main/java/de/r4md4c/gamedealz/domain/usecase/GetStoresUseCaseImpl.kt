package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.repository.StoresRepository
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.network.repository.StoresRemoteRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class GetStoresUseCaseImpl(
    private val storesRemoteRepository: StoresRemoteRepository,
    private val storesRepository: StoresRepository
) : GetStoresUseCase {

    override suspend fun invoke(activeRegion: ActiveRegion): List<Store> = withContext(IO) {
        val remoteStores = storesRemoteRepository.stores(activeRegion.region.regionCode, activeRegion.country.code)

        storesRepository.save(remoteStores.map { Store(it.id, it.title, it.color) })

        storesRepository.all()
    }
}