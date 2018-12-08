package de.r4md4c.gamedealz.network.service

import com.google.common.truth.Truth.assertThat
import de.r4md4c.gamedealz.network.NETWORK
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest

class IsThereAnyDealServiceTest : KoinTest {

    private val service: IsThereAnyDealService by inject()

    @Before
    fun beforeEach() {
        startKoin(listOf(NETWORK))
    }

    @Test
    fun `all regions`() {
        runBlocking {
            val result = service.regions().await()

            assertThat(result.data.size).isGreaterThan(0)
        }
    }

    @Test
    fun stores() {
        runBlocking {
            val result = service.stores("us", "US").await()

            assertThat(result.data).isNotEmpty()
        }
    }

    @Test
    fun plain() {
        runBlocking {
            val result = service.plain(shop = "steam", gameId = "app/377160").await()

            assertThat(result.data.value).isEqualTo("falloutiv")
        }
    }

    @Test
    fun allPlains() {
        runBlocking {
            val result = service.allPlains(shops = setOf("discord")).await()

            assertThat(result.data).isNotEmpty()
        }
    }

    @Test
    fun prices() {
        runBlocking {
            val result = service.prices(plains = setOf("battlefieldv")).await()

            assertThat(result.data).isNotEmpty()
            assertThat(result.data["battlefieldv"]!!.list).isNotEmpty()
        }
    }

    @After
    fun afterEach() {
        stopKoin()
    }

}