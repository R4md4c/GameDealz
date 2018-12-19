package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.gamedealz.data.repository.StoresRepository
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.model.toStoreModel
import de.r4md4c.gamedealz.domain.usecase.GetSelectedStoresUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.withContext

class GetSelectedStoresUseCaseImpl(private val storesRepository: StoresRepository) : GetSelectedStoresUseCase {

    override suspend fun invoke(param: VoidParameter?): ReceiveChannel<List<StoreModel>> =
        withContext(IO) {
            storesRepository.selectedStores().map { it.map { store -> store.toStoreModel() } }
        }
}