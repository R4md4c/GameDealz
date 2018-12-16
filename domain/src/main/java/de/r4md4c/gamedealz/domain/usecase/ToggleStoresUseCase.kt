package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.CollectionParameter
import de.r4md4c.gamedealz.domain.model.StoreModel

/**
 * Select a stores that will affect the scope of searching for deals and games.
 */
interface ToggleStoresUseCase : UseCase<CollectionParameter<StoreModel>, Unit>
