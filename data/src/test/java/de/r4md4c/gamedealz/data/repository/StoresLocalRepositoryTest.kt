package de.r4md4c.gamedealz.data.repository

import de.r4md4c.gamedealz.data.dao.StoresDao
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class StoresLocalRepositoryTest {

    @Mock
    private lateinit var storesDao: StoresDao

    private lateinit var storesLocalRepository: StoresLocalDataSourceImpl

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        storesLocalRepository = StoresLocalDataSourceImpl(storesDao)
    }

    @Test
    fun `all invokes all in dao when no collection is supplied`() {
        runBlocking {
            ArrangeBuilder()
            storesLocalRepository.all()

            verify(storesDao).all()
        }
    }

    @Test
    fun `all invokes all in dao when collection is supplied`() {
        runBlocking {
            ArrangeBuilder()
            storesLocalRepository.all(emptyList())

            verify(storesDao).all(any())
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

    inner class ArrangeBuilder {
        init {
            whenever(storesDao.all()).thenReturn(flowOf(emptyList()))
            whenever(storesDao.all(any())).thenReturn(flowOf(emptyList()))
        }
    }
}