package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.entity.Store
import kotlinx.coroutines.channels.ReceiveChannel

interface StoresRepository : Repository<Store, String> {

    /**
     * Updates the stores according to the selected state.
     *
     * @param selected true or false
     * @param stores the stores to be updated.
     */
    fun updateSelected(selected: Boolean, stores: Set<Store>)

    /**
     * Gets the selected stores from the database.
     *
     * @return A collection of selected stores.
     */
    suspend fun selectedStores(): ReceiveChannel<Collection<Store>>

    /**
     * Clears the repository then insert the stores.
     *
     * @param stores the stores that are going to be stored after clearing the table.
     */
    suspend fun replace(stores: Collection<Store>)
}