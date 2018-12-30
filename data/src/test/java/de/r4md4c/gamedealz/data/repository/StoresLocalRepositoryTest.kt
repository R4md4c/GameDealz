package de.r4md4c.gamedealz.data.repository

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.data.dao.StoresDao
import io.reactivex.Flowable
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
            whenever(storesDao.all()).thenReturn(Flowable.just(emptyList()))
            whenever(storesDao.all(any())).thenReturn(Flowable.just(emptyList()))
        }
    }
}