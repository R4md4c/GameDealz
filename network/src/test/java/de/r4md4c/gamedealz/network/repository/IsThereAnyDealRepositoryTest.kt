package de.r4md4c.gamedealz.network.repository

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import de.r4md4c.gamedealz.network.model.*
import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import de.r4md4c.gamedealz.network.service.PlainPriceList
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
    internal lateinit var service: IsThereAnyDealService

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

    @Test
    fun deals() {
        runBlocking {
            whenever(service.deals(any(), any(), any(), any(), any(), any()))
                .thenReturn(async { DataWrapper<ListWrapper<Deal>>(ListWrapper(emptyList(), 0)) })

            subject.deals(0, 0, "region", "country", setOf("steam", "gog"))

            verify(service).deals(any(), any(), any(), eq("region"), eq("country"), eq("steam,gog"))
        }
    }

    @Test
    fun retrievesPrices() {
        runBlocking {
            val response: DataWrapper<PlainPriceList> =
                DataWrapper(mapOf("battlefield" to ListWrapper(listOf(mock<Price>(), mock<Price>()), 0)))
            whenever(service.prices(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
                .thenReturn(async { response })

            val result = subject.retrievesPrices(setOf("plain1", "plain2"))

            verify(service).prices(any(), eq("plain1,plain2"), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            assertThat(result).containsEntry("battlefield", response.data["battlefield"]!!.list)
        }
    }
}