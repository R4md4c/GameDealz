package de.r4md4c.gamedealz.data.repository

import com.nhaarman.mockitokotlin2.verify
import de.r4md4c.gamedealz.data.dao.StoresDao
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class StoresLocalRepositoryTest {

    @Mock
    private lateinit var storesDao: StoresDao

    private lateinit var storesLocalRepository: StoresLocalRepository

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        storesLocalRepository = StoresLocalRepository(storesDao)
    }

    @Test
    fun all() {
        runBlocking {
            storesLocalRepository.all()

            verify(storesDao).all()
        }
    }

    @Test
    fun save() {
        runBlocking {
            storesLocalRepository.save(emptyList())

            verify(storesDao).insert(emptyList())
        }
    }

    @Test
    fun findById() {
        runBlocking {
            storesLocalRepository.findById("")

            verify(storesDao).singleStore("")
        }
    }
}