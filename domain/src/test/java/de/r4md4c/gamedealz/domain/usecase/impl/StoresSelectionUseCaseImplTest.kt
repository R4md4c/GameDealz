package de.r4md4c.gamedealz.domain.usecase.impl

import com.google.common.truth.Truth.assertThat
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.StoreModel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class StoresSelectionUseCaseImplTest {

    private lateinit var testSubject: StoresSelectionUseCaseImpl

    @Before
    fun beforeEach() {
        testSubject = StoresSelectionUseCaseImpl()
    }

    @Test
    fun `it should accumulate stores through the channel`() {
        runBlocking {
            val storesChannel = testSubject()

            testSubject(TypeParameter(store()))
            testSubject(TypeParameter(store().copy("id")))
            testSubject(TypeParameter(store().copy("id2")))

            storesChannel.consume {
                assertThat(this.receive()).isEqualTo(setOf(store(), store().copy("id"), store().copy("id2")))
            }
        }
    }

    private fun store() = StoreModel("", "name")
}