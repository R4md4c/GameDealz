package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.entity.Store

interface StoresRepository : Repository<Store, String> {

    /**
     * Updates the stores according to the selected state.
     *
     * @param selected true or false
     * @param stores the stores to be updated.
     */
    fun updateSelected(selected: Boolean, stores: Set<Store>)

}