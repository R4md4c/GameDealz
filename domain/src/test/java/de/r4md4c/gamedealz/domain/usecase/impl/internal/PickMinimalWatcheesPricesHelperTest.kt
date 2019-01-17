/*
 * This file is part of GameDealz.
 *
 * GameDealz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * GameDealz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GameDealz.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.r4md4c.gamedealz.domain.usecase.impl.internal

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.commonproviders.date.DateProvider
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.data.entity.WatcheeWithStores
import de.r4md4c.gamedealz.data.repository.WatchlistRepository
import de.r4md4c.gamedealz.data.repository.WatchlistStoresRepository
import de.r4md4c.gamedealz.network.model.Price
import de.r4md4c.gamedealz.network.model.Shop
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit

class PickMinimalWatcheesPricesHelperTest {

    @Mock
    private lateinit var watchlistRepository: WatchlistRepository

    @Mock
    private lateinit var watchlistStoresRepository: WatchlistStoresRepository

    @Mock
    private lateinit var dateProvider: DateProvider

    private lateinit var helper: PickMinimalWatcheesPricesHelper

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        helper = PickMinimalWatcheesPricesHelper(watchlistRepository, watchlistStoresRepository, dateProvider)
    }


    @Test
    fun `it should return empty map when find by returns null`() {
        runBlocking {
            ArrangeBuilder()
                .withWatchlistRepositoryFindByIdResultNotFound()

            assertThat(helper.pick(mapOf("plainId" to listOf(PRICE)))).isEmpty()
        }
    }

    @Test
    fun `it should return empty map when when stores in a watchee is empty null`() {
        runBlocking {
            ArrangeBuilder()
                .withWatchlistRepositoryFindByIdResult("plainId", WATCHEE)
                .withFindWatcheeWithStoresResult(WATCHEE, WATCHEE_WITH_STORES.copy(stores = emptySet()))

            assertThat(helper.pick(mapOf("plainId" to listOf(PRICE)))).isEmpty()
        }
    }

    @Test
    fun `it should return empty map when when retrieved prices does not include the store in the watchee`() {
        runBlocking {
            ArrangeBuilder()
                .withWatchlistRepositoryFindByIdResult("plainId", WATCHEE)
                .withFindWatcheeWithStoresResult(
                    WATCHEE,
                    WATCHEE_WITH_STORES.copy(stores = setOf(STORE.copy(id = "not found")))
                )

            assertThat(helper.pick(mapOf("plainId" to listOf(PRICE, PRICE.copy(shop = Shop("2", "")))))).isEmpty()
        }
    }

    @Test
    fun `it should return empty map when the target price has not yet reached`() {
        runBlocking {
            val targetPrice = 6f
            val minimumPrice = 10f
            val retrievedPrices = mapOf("plainId" to listOf(PRICE.copy(newPrice = minimumPrice)))
            ArrangeBuilder()
                .withWatchlistRepositoryFindByIdResult("plainId", WATCHEE.copy(targetPrice = targetPrice))
                .withFindWatcheeWithStoresResult(WATCHEE.copy(targetPrice = targetPrice), WATCHEE_WITH_STORES)

            assertThat(helper.pick(retrievedPrices)).isEmpty()

        }
    }

    @Test
    fun `it should return non empty map when the target price is equal to the threshold price`() {
        runBlocking {
            val targetPrice = 6f
            val minimumPrice = 6f
            val retrievedPrices = mapOf("plainId" to listOf(PRICE.copy(newPrice = minimumPrice)))
            ArrangeBuilder()
                .withWatchlistRepositoryFindByIdResult("plainId", WATCHEE.copy(targetPrice = targetPrice))
                .withFindWatcheeWithStoresResult(WATCHEE.copy(targetPrice = targetPrice), WATCHEE_WITH_STORES)

            val result = helper.pick(retrievedPrices)

            assertThat(result).isNotEmpty()
            assertThat(result).isEqualTo(mapOf(PRICE.copy(newPrice = minimumPrice) to WATCHEE.copy(targetPrice = targetPrice)))

        }
    }

    @Test
    fun `it should return non empty map when the minimal price is less than the threshold price`() {
        runBlocking {
            val targetPrice = 6f
            val minimumPrice = 4f
            val retrievedPrices = mapOf("plainId" to listOf(PRICE.copy(newPrice = minimumPrice)))
            ArrangeBuilder()
                .withWatchlistRepositoryFindByIdResult("plainId", WATCHEE.copy(targetPrice = targetPrice))
                .withFindWatcheeWithStoresResult(WATCHEE.copy(targetPrice = targetPrice), WATCHEE_WITH_STORES)

            val result = helper.pick(retrievedPrices)

            assertThat(result).isNotEmpty()
            assertThat(result).isEqualTo(mapOf(PRICE.copy(newPrice = minimumPrice) to WATCHEE.copy(targetPrice = targetPrice)))

        }
    }

    @Test
    fun `it should update the watchees with the last check time in seconds and minimum price`() {
        runBlocking {
            val retrievedPrices = mapOf(
                "plainId" to listOf(
                    PRICE.copy(newPrice = 5f),
                    PRICE.copy(newPrice = 6f),
                    PRICE.copy(newPrice = 7f)
                )
            )
            val currentTime = System.currentTimeMillis()
            ArrangeBuilder()
                .withWatchlistRepositoryFindByIdResult("plainId", WATCHEE)
                .withFindWatcheeWithStoresResult(WATCHEE, WATCHEE_WITH_STORES)
                .withDateProvider(currentTime)

            helper.pick(retrievedPrices)

            verify(watchlistRepository).updateWatchee(1, 5f, "", TimeUnit.MILLISECONDS.toSeconds(currentTime))
        }
    }

    @Test
    fun `whole flow with two values`() {
        runBlocking {
            val retrievedPrices = mapOf(
                "plainId" to listOf(PRICE.copy(newPrice = 5f), PRICE.copy(newPrice = 6f), PRICE.copy(newPrice = 7f)),
                "plainId2" to listOf(PRICE.copy(newPrice = 5f), PRICE.copy(newPrice = 6f), PRICE.copy(newPrice = 7f))
            )
            val currentTime = System.currentTimeMillis()
            ArrangeBuilder()
                .withWatchlistRepositoryFindByIdResult("plainId", WATCHEE.copy(id = 1, targetPrice = 10f))
                .withWatchlistRepositoryFindByIdResult("plainId2", WATCHEE.copy(id = 2, targetPrice = 6f))
                .withFindWatcheeWithStoresResult(WATCHEE.copy(id = 1, targetPrice = 10f), WATCHEE_WITH_STORES)
                .withFindWatcheeWithStoresResult(WATCHEE.copy(id = 2, targetPrice = 6f), WATCHEE_WITH_STORES)
                .withDateProvider(currentTime)

            val result = helper.pick(retrievedPrices)

            assertThat(result).isNotEmpty()
            assertThat(result).isEqualTo(
                mapOf(
                    PRICE.copy(newPrice = 5f) to WATCHEE.copy(id = 1, targetPrice = 10f),
                    PRICE.copy(newPrice = 5f) to WATCHEE.copy(id = 2, targetPrice = 6f)
                )
            )
        }
    }

    @Test
    fun `whole flow with two values and one of them is not found`() {
        runBlocking {
            val retrievedPrices = mapOf(
                "plainId" to listOf(PRICE.copy(newPrice = 5f), PRICE.copy(newPrice = 6f), PRICE.copy(newPrice = 7f)),
                "plainId2" to listOf(PRICE.copy(newPrice = 5f), PRICE.copy(newPrice = 6f), PRICE.copy(newPrice = 7f))
            )
            val currentTime = System.currentTimeMillis()
            ArrangeBuilder()
                .withWatchlistRepositoryFindByIdResult("plainId", WATCHEE.copy(id = 1, targetPrice = 10f))
                .withWatchlistRepositoryFindByIdResultNotFound("plainId2")
                .withFindWatcheeWithStoresResult(WATCHEE.copy(id = 1, targetPrice = 10f), WATCHEE_WITH_STORES)
                .withDateProvider(currentTime)

            val result = helper.pick(retrievedPrices)

            assertThat(result).isNotEmpty()
            assertThat(result).isEqualTo(mapOf(PRICE.copy(newPrice = 5f) to WATCHEE.copy(id = 1, targetPrice = 10f)))
        }
    }

    inner class ArrangeBuilder {

        fun withWatchlistRepositoryFindByIdResult(key: String, result: Watchee?) = apply {
            runBlocking {
                whenever(watchlistRepository.findById(eq(key))).thenReturn(produce(capacity = 1) { send(result) })
            }
        }

        fun withWatchlistRepositoryFindByIdResultNotFound(key: String? = null) = apply {
            runBlocking {
                whenever(
                    watchlistRepository.findById(
                        key ?: anyOrNull()
                    )
                ).thenReturn(produce(capacity = 1) { send(null) })
            }
        }

        fun withFindWatcheeWithStoresResult(watchee: Watchee?, result: WatcheeWithStores?) = apply {
            runBlocking {
                whenever(watchlistStoresRepository.findWatcheeWithStores(watchee ?: anyOrNull())).thenReturn(result)
            }
        }

        fun withDateProvider(timestamp: Long) {
            whenever(dateProvider.timeInMillis()).thenReturn(timestamp)
        }
    }

    private companion object {
        val PRICE = Price(1f, 1f, 2, "", Shop("1", ""), emptySet())

        val WATCHEE = Watchee(1, "plainId", "", 0, 0, 0f, "", 0f, "", "", "")

        val STORE = Store("1", "", "")

        val WATCHEE_WITH_STORES = WatcheeWithStores(WATCHEE, (1..5).map { STORE.copy("$it") }.toSet())
    }
}