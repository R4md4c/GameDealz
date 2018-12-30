package de.r4md4c.gamedealz.domain.usecase.impl

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.repository.StoresRepository
import de.r4md4c.gamedealz.domain.CollectionParameter
import de.r4md4c.gamedealz.domain.model.StoreModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ToggleStoresUseCaseImplTest {

    @Mock
    private lateinit var storesRepository: StoresRepository

    private lateinit var testSubject: ToggleStoresUseCaseImpl

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        testSubject = ToggleStoresUseCaseImpl(storesRepository)
    }

    @Test
    fun `it should toggle the selection flags for all supplied stores`() {
        runBlocking {
            val storesModel = listOf(store("id0"), store("id1"), store("id2"))
            val storesModelWithSelectedSet = listOf(
                storesModel[0].copy(selected = true),
                storesModel[1].copy(selected = true), storesModel[2].copy(selected = false)
            )
            ArrangeBuilder()
                .withStoredStores(storesModelWithSelectedSet.map { Store(it.id, it.name, "", selected = it.selected) })

            testSubject(CollectionParameter(storesModelWithSelectedSet))

            verify(storesRepository).updateSelected(
                false,
                setOf(Store("id0", "name", "", true), Store("id1", "name", "", true))
            )
            verify(storesRepository).updateSelected(
                true,
                setOf(Store("id2", "name", "", false))
            )
        }
    }

    private fun store(id: String): StoreModel = StoreModel(id, "name", false)

    inner class ArrangeBuilder {

        fun withStoredStores(stores: List<Store>) = apply {
            runBlocking {
                whenever(storesRepository.all(any())).thenReturn(produce(capacity = Channel.UNLIMITED) { offer(stores) })
            }
        }
    }
}