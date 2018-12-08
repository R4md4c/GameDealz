package de.r4md4c.gamedealz.data.repository

/**
 * Base interface that will be extended by all repos.
 */
interface Repository<Model, PrimaryKey> {

    /**
     * Retrieves all models from the store.
     */
    suspend fun all(): List<Model>

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
