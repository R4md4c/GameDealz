package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.repository.StoresRepository
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.network.repository.StoresRemoteRepository
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.channels.map

internal class GetStoresUseCaseImpl(
    private val storesRemoteRepository: StoresRemoteRepository,
    private val storesRepository: StoresRepository,
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase
) : GetStoresUseCase {

    override suspend fun invoke(param: TypeParameter<ActiveRegion>?): ReceiveChannel<List<StoreModel>> {
        val activeRegion = param?.value ?: activeRegionUseCase()

        return with(activeRegion) {
            val stores = storesRepository.all().first()

            if (stores.isEmpty()) {
                val remoteStores = storesRemoteRepository.stores(regionCode, country.code)
                storesRepository.save(remoteStores.map { Store(it.id, it.title, it.color) })
            }

            storesRepository.all().map { it.map { item -> StoreModel(item.id, item.name, item.selected) } }
        }
    }
}