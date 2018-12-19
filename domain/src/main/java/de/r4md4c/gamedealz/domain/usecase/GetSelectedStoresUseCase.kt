package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.StoreModel
import kotlinx.coroutines.channels.ReceiveChannel

interface GetSelectedStoresUseCase : UseCase<VoidParameter, ReceiveChannel<List<StoreModel>>>