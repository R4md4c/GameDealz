package de.r4md4c.gamedealz.network.repository

import de.r4md4c.gamedealz.network.model.AccessToken
import de.r4md4c.gamedealz.network.model.DataWrapper
import de.r4md4c.gamedealz.network.model.ListWrapper
import de.r4md4c.gamedealz.network.model.PriceDTO
import de.r4md4c.gamedealz.network.model.Stores
import de.r4md4c.gamedealz.network.model.User
import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import de.r4md4c.gamedealz.network.service.PlainPriceList
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
    fun regions() = runBlockingTest {
        whenever(service.regions()).thenReturn(DataWrapper(mapOf()))

        val result = subject.regions()

        verify(service).regions()
        assertThat(result).isNotNull()
    }

    @Test
    fun stores() = runBlockingTest {

        whenever(service.stores("", "")).thenReturn(Stores(emptyList()))

        val result = subject.stores("", "")

        verify(service).stores("", "")
        assertThat(result).isEmpty()
    }

    @Test
    fun deals() = runBlockingTest {
        whenever(service.deals(any(), any(), any(), any(), any(), any()))
            .thenReturn(DataWrapper(ListWrapper(emptyList(), 0)))

        subject.deals(0, 0, "region", "country", setOf("steam", "gog"))

        verify(service).deals(any(), any(), any(), eq("region"), eq("country"), eq("steam,gog"))
    }

    @Test
    fun retrievesPrices() = runBlockingTest {
        val response: DataWrapper<PlainPriceList> =
            DataWrapper(
                mapOf(
                    "battlefield" to ListWrapper(
                        listOf(mock<PriceDTO>(), mock<PriceDTO>()),
                        0
                    )
                )
            )
        whenever(
            service.prices(
                any(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        )
            .thenReturn(response)

        val result = subject.retrievesPrices(setOf("plain1", "plain2"))

        verify(service).prices(
            any(),
            eq("plain1,plain2"),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull()
        )
        assertThat(result).containsEntry("battlefield", response.data["battlefield"]!!.list)
    }

    @Test
    fun `user when user name is not null`() = runBlockingTest {
        whenever(service.userInfo(any())).thenReturn(
            DataWrapper(mapOf("username" to "John Smith"))
        )

        val result = subject.user(AccessToken("A token"))

        assertThat(result).isEqualTo(User.KnownUser("John Smith"))
    }

    @Test
    fun `user when user name is null`() = runBlockingTest {
        whenever(service.userInfo(any())).thenReturn(
            DataWrapper(emptyMap())
        )

        val result = subject.user(AccessToken("A token"))

        assertThat(result).isEqualTo(User.UnknownUser)
    }
}