package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.dao.PlainsDao
import de.r4md4c.gamedealz.data.entity.Plain
import kotlinx.coroutines.channels.ReceiveChannel

internal class PlainsLocalRepository(private val plainsDao: PlainsDao) : PlainsRepository {

    override suspend fun all(ids: Collection<String>?): ReceiveChannel<List<Plain>> {
        throw UnsupportedOperationException("PlainsDao doesn't support retrieving full list")
    }

    override suspend fun count(): Int = plainsDao.count()

    override suspend fun save(models: List<Plain>) {
        plainsDao.insert(models)
    }

    override suspend fun findById(id: String): Plain? = plainsDao.findOne(id)
}