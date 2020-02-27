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

package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.dao.StoresDao
import de.r4md4c.gamedealz.data.entity.Store
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class StoresLocalDataSourceImpl @Inject constructor(
    private val storesDao: StoresDao
) : StoresLocalDataSource {

    override suspend fun all(ids: Collection<String>?): Flow<List<Store>> =
        (ids?.let { storesDao.all(it.toSet()) } ?: storesDao.all())

    override suspend fun save(models: List<Store>) = storesDao.insert(models)

    override suspend fun findById(id: String): Store? = storesDao.singleStore(id)

    override fun updateSelected(selected: Boolean, stores: Set<Store>) {
        storesDao.updateSelected(selected, stores.mapTo(mutableSetOf()) { it.id })
    }

    override suspend fun replace(stores: Collection<Store>) {
        storesDao.replaceAll(stores)
    }

    override suspend fun selectedStores(): Flow<Collection<Store>> =
        storesDao.allSelected()
}
