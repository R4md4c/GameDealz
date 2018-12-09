package de.r4md4c.gamedealz.network.repository

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.network.model.DataWrapper
import de.r4md4c.gamedealz.network.model.Stores
import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import de.r4md4c.gamedealz.network.service.RegionCodes
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class IsThereAnyDealRepositoryTest {

    private lateinit var subject: IsThereAnyDealRepository

    @Mock
    lateinit var service: IsThereAnyDealService

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        subject = IsThereAnyDealRepository(service)
    }

    @Test
    fun regions() {
        runBlocking {
            whenever(service.regions()).thenReturn(async { DataWrapper<RegionCodes>(mapOf()) })

            val result = subject.regions()

            verify(service).regions()
            assertThat(result).isNotNull()
        }
    }

    @Test
    fun stores() {
        runBlocking {
            whenever(service.stores("", "")).thenReturn(async { Stores(emptyList()) })

            val result = subject.stores("", "")

            verify(service).stores("", "")
            assertThat(result).isEmpty()
        }
    }
}