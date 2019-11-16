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

import de.r4md4c.commonproviders.coroutines.GameDealzDispatchers.IO
import de.r4md4c.gamedealz.data.repository.StoresRepository
import de.r4md4c.gamedealz.domain.CollectionParameter
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.usecase.ToggleStoresUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal class ToggleStoresUseCaseImpl(private val storesRepository: StoresRepository) : ToggleStoresUseCase {

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