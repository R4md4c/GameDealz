package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.gamedealz.data.repository.StoresRepository
import de.r4md4c.gamedealz.domain.CollectionParameter
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.usecase.ToggleStoresUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.withContext

internal class StoresSelectionUseCaseImpl(private val storesRepository: StoresRepository) : ToggleStoresUseCase {

    override suspend fun invoke(param: CollectionParameter<StoreModel>?) {
        requireNotNull(param)

        val allStores = withContext(IO) {
            storesRepository.all(param.list.map { it.id }).first()
        }

        val storesToBeSelected = allStores.filter { !it.selected }
        val storesToBeDeselected = allStores.filter { it.selected }

        withContext(IO) {
            storesRepository.updateSelected(true, storesToBeSelected.toSet())
            storesRepository.updateSelected(false, storesToBeDeselected.toSet())
        }
    }

}