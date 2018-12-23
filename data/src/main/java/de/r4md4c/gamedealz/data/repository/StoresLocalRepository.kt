package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.dao.StoresDao
import de.r4md4c.gamedealz.data.entity.Store
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.reactive.openSubscription

internal class StoresLocalRepository(private val storesDao: StoresDao) : StoresRepository {

    override suspend fun all(ids: Collection<String>?): ReceiveChannel<List<Store>> =
        (ids?.let { storesDao.all(it.toSet()) } ?: storesDao.all()).openSubscription()

    override suspend fun save(models: List<Store>) = storesDao.insert(models)

    override suspend fun findById(id: String): Store? = storesDao.singleStore(id)

    override fun updateSelected(selected: Boolean, stores: Set<Store>) {
        storesDao.updateSelected(selected, stores.mapTo(mutableSetOf()) { it.id })
    }

    override suspend fun replace(stores: Collection<Store>) {
        storesDao.replaceAll(stores)
    }

    override suspend fun selectedStores(): ReceiveChannel<Collection<Store>> =
        storesDao.allSelected().openSubscription()
}