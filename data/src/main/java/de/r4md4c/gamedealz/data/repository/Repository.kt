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

import kotlinx.coroutines.flow.Flow

/**
 * Base interface that will be extended by all repos.
 */
interface Repository<Model, PrimaryKey> {

    /**
     * Retrieves all models from the store.
     *
     * @param ids An optional collection of ids that needs to be retrieved, if null, all is going to be retrieved.
     */
    suspend fun all(ids: Collection<PrimaryKey>? = null): Flow<List<Model>>

    /**
     * Saves all models to the store.
     *
     * @param models the models to be saved.
     */
    suspend fun save(models: List<Model>)

    /**
     * Finds a single model by id.
     *
     * @param id the id that will be used to retrieve the model form.
     */
    suspend fun findById(id: PrimaryKey): Model?

}
