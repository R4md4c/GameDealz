package de.r4md4c.gamedealz.data.repository

import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Base interface that will be extended by all repos.
 */
interface Repository<Model, PrimaryKey> {

    /**
     * Retrieves all models from the store.
     *
     * @param ids An optional collection of ids that needs to be retrieved, if null, all is going to be retrieved.
     */
    suspend fun all(ids: Collection<PrimaryKey>? = null): ReceiveChannel<List<Model>>

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
