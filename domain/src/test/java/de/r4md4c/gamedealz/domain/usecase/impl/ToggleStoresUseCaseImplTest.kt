package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.repository.StoresLocalDataSource
import de.r4md4c.gamedealz.domain.CollectionParameter
import de.r4md4c.gamedealz.domain.model.StoreModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ToggleStoresUseCaseImplTest {

    @Mock
    private lateinit var storesRepository: StoresLocalDataSource

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
                whenever(storesRepository.all(any())).thenReturn(flowOf(stores))
            }
        }
    }
}