package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.StoreModel
import kotlinx.coroutines.channels.ReceiveChannel


interface GetStoresUseCase : UseCase<TypeParameter<ActiveRegion>, ReceiveChannel<List<StoreModel>>> {

    /**
     * Retrieves Stores according to the active region.
     *
     * @param param The active region to search for, if null is supplied, then we try to retrieved the stores, according
     * to the current stored region.
     * @return a List of Store Models.
     */
    override suspend fun invoke(param: TypeParameter<ActiveRegion>?): ReceiveChannel<List<StoreModel>>
}

