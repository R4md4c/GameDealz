package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.dao.StoresDao
import de.r4md4c.gamedealz.data.entity.Store

internal class StoresLocalRepository(private val storesDao: StoresDao) : StoresRepository {

    override suspend fun all(): List<Store> = storesDao.all()

    override suspend fun save(models: List<Store>) = storesDao.insert(models)

    override suspend fun findById(id: String): Store? = storesDao.singleStore(id)

}