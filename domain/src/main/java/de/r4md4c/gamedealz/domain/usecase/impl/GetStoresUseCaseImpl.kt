/*
 * This file is part of GameDealz.
 *
 * GameDealz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * GameDealz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GameDealz.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.repository.StoresLocalDataSource
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.network.repository.StoresRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class GetStoresUseCaseImpl @Inject constructor(
    private val storesRemoteRepository: StoresRemoteRepository,
    private val storesRepository: StoresLocalDataSource,
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase
) : GetStoresUseCase {

    override fun invoke(param: ActiveRegion?): Flow<List<StoreModel>> = flow {
        val activeRegion = param ?: activeRegionUseCase.invoke()

        with(activeRegion) {
            val stores = storesRepository.all().first()

            if (stores.isEmpty()) {
                val remoteStores = storesRemoteRepository.stores(regionCode, country.code)
                storesRepository.save(remoteStores.map { Store(it.id, it.title, it.color) })
            }

            emitAll(
                storesRepository.all()
                    .map { it.map { item -> StoreModel(item.id, item.name, item.selected) } }
            )
        }
    }
}
