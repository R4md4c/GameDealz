package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.domain.model.ActiveRegion

/**
 * Retrieves the list of stores under specific regions.
 */
interface GetStoresUseCase {

    suspend operator fun invoke(activeRegion: ActiveRegion): List<Store>
}