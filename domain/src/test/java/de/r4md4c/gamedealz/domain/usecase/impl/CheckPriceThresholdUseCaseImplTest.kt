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

package de.r4md4c.gamedealz.domain.usecase.impl

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.commonproviders.date.DateProvider
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.data.entity.WatcheeWithStores
import de.r4md4c.gamedealz.data.repository.WatchlistRepository
import de.r4md4c.gamedealz.data.repository.WatchlistStoresRepository
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.CountryModel
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.network.model.Price
import de.r4md4c.gamedealz.network.model.Shop
import de.r4md4c.gamedealz.network.repository.PricesRemoteRepository
import de.r4md4c.gamedealz.test.TestTransactor
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit

class CheckPriceThresholdUseCaseImplTest {

    @Mock
    private lateinit var watchlistRepository: WatchlistRepository

    @Mock
    private lateinit var watchlistStoresRepository: WatchlistStoresRepository

    @Mock
    private lateinit var pricesRemoteRepository: PricesRemoteRepository

    @Mock
    private lateinit var currentActiveRegionUseCase: GetCurrentActiveRegionUseCase

    @Mock
    private lateinit var dateProvider: DateProvider

    private lateinit var subject: CheckPriceThresholdUseCaseImpl

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        subject = CheckPriceThresholdUseCaseImpl(
            watchlistRepository,
            watchlistStoresRepository,
            pricesRemoteRepository,
            currentActiveRegionUseCase,
            TestTransactor,
            dateProvider
        )
    }


    @Test
    fun `it should return emptySet when allWatcheesWithStores are empty`() {
        runBlocking {
            ArrangeBuilder()
                .withWatcheesWithStores(emptyList())

            assertThat(subject.invoke()).isEmpty()
        }
    }

    @Test
    fun `it should return emptySet when all the watchee target price is equal to current price are empty`() {
        runBlocking {
            ArrangeBuilder()
                .withWatcheesWithStores((1..5).map {
                    WATCHEES_WITH_STORES.copy(
                        watchee = WATCHEE.copy(
                            currentPrice = 5f,
                            targetPrice = 5f
                        )
                    )
                })

            assertThat(subject.invoke()).isEmpty()
        }
    }

    @Test
    fun `it should return emptySet when all the watchees target price are greater than current price are empty`() {
        runBlocking {
            ArrangeBuilder()
                .withWatcheesWithStores((1..5).map {
                    WATCHEES_WITH_STORES.copy(
                        watchee = WATCHEE.copy(
                            currentPrice = 5f,
                            targetPrice = 6f
                        )
                    )
                })

            assertThat(subject.invoke()).isEmpty()
        }
    }

    @Test
    fun `it should return emptySet when the retrieved prices are empty`() {
        runBlocking {
            ArrangeBuilder()
                .withWatcheesWithStores((1..5).map {
                    WATCHEES_WITH_STORES.copy(
                        watchee = WATCHEE.copy(
                            currentPrice = 10f,
                            targetPrice = 6f
                        )
                    )
                })
                .withRetrievedPrices(emptyMap())

            assertThat(subject.invoke()).isEmpty()
        }
    }

    @Test
    fun `it should return emptySet when the retrieved prices has empty price models`() {
        runBlocking {
            ArrangeBuilder()
                .withWatcheesWithStores((1..5).map {
                    WATCHEES_WITH_STORES.copy(
                        watchee = WATCHEE.copy(
                            currentPrice = 10f,
                            targetPrice = 6f
                        )
                    )
                })
                .withRetrievedPrices(mapOf("plainId" to emptyList()))

            assertThat(subject.invoke()).isEmpty()
        }
    }

    @Test
    fun `it should return emptySet when the retrieved prices has not yet reached the target price`() {
        runBlocking {
            ArrangeBuilder()
                .withWatcheesWithStores((1..5).map {
                    WATCHEES_WITH_STORES.copy(
                        watchee = WATCHEE.copy(
                            currentPrice = 10f,
                            targetPrice = 6f
                        )
                    )
                })
                .withRetrievedPrices(mapOf("plainId" to listOf(PRICE.copy(newPrice = 10f))))

            assertThat(subject.invoke()).isEmpty()
        }
    }

    @Test
    fun `it should not return emptySet when the retrieved prices is equal the reached the target price`() {
        runBlocking {
            val targetWatchee = WATCHEE.copy(currentPrice = 10f, targetPrice = 6f)
            ArrangeBuilder()
                .withWatcheesWithStores((1..5).map { WATCHEES_WITH_STORES.copy(watchee = targetWatchee) })
                .withRetrievedPrices(mapOf("plainId" to listOf(PRICE.copy(newPrice = 6f))))
                .withFoundWatcheeInRepository("plainId", targetWatchee)
                .withWatchlistAllResult(setOf(targetWatchee.id), listOf(targetWatchee))

            assertThat(subject.invoke()).isNotEmpty()
        }
    }


    @Test
    fun `it should always update the lastTimeCheck and lastChecked of the checked watchees`() {
        runBlocking {
            val targetWatchee = WATCHEE.copy(currentPrice = 10f, targetPrice = 6f)
            val currentTimeInMillis = System.currentTimeMillis()
            ArrangeBuilder()
                .withWatcheesWithStores((1..5).map { WATCHEES_WITH_STORES.copy(watchee = targetWatchee) })
                .withRetrievedPrices(mapOf("plainId" to listOf(PRICE.copy(newPrice = 10f))))
                .withFoundWatcheeInRepository("plainId", targetWatchee)
                .withWatchlistAllResult(setOf(targetWatchee.id), listOf(targetWatchee))
                .withCurrentTime(currentTimeInMillis)

            subject.invoke()

            verify(watchlistRepository).updateWatchee(1L, 10f, TimeUnit.MILLISECONDS.toSeconds(currentTimeInMillis))
        }
    }

    @Test
    fun `it should not return emptySet when the retrieved prices is less than target price`() {
        runBlocking {
            val targetWatchee = WATCHEE.copy(currentPrice = 10f, targetPrice = 6f)
            ArrangeBuilder()
                .withWatcheesWithStores((1..5).map { WATCHEES_WITH_STORES.copy(watchee = targetWatchee) })
                .withRetrievedPrices(mapOf("plainId" to listOf(PRICE.copy(newPrice = 5f))))
                .withFoundWatcheeInRepository("plainId", targetWatchee)
                .withWatchlistAllResult(setOf(targetWatchee.id), listOf(targetWatchee))

            assertThat(subject.invoke()).isNotEmpty()
        }
    }

    inner class ArrangeBuilder {
        init {
            runBlocking {
                whenever(currentActiveRegionUseCase.invoke(anyOrNull())).thenReturn(ACTIVE_REGION)
                whenever(watchlistRepository.findById(any<String>())).thenReturn(produce(capacity = 1) { close() })
            }
        }

        fun withWatcheesWithStores(watcheeWithStores: List<WatcheeWithStores>) = apply {
            runBlocking {
                whenever(watchlistStoresRepository.allWatcheesWithStores()).thenReturn(watcheeWithStores)
            }
        }

        fun withRetrievedPrices(plainIdsPriceMap: Map<String, List<Price>>) = apply {
            runBlocking {
                whenever(
                    pricesRemoteRepository.retrievesPrices(
                        anyOrNull(),
                        anyOrNull(),
                        anyOrNull(),
                        anyOrNull(),
                        anyOrNull()
                    )
                )
                    .thenReturn(plainIdsPriceMap)
            }
        }

        fun withFoundWatcheeInRepository(plainId: String, returnedWatchee: Watchee) = apply {
            runBlocking {
                whenever(watchlistRepository.findById(plainId)).thenReturn(produce(capacity = 1) { send(returnedWatchee) })
            }
        }

        fun withWatchlistAllResult(ids: Set<Long>, result: List<Watchee>) = apply {
            runBlocking {
                whenever(watchlistRepository.all(ids.toList())).thenReturn(produce(capacity = 1) { send(result) })
            }
        }

        fun withCurrentTime(currentTimeInMillis: Long) = apply {
            whenever(dateProvider.timeInMillis()).thenReturn(currentTimeInMillis)
        }
    }

    private companion object {
        val ACTIVE_REGION = ActiveRegion("US", CountryModel("US"), CurrencyModel("", ""))

        val WATCHEE = Watchee(1, "plainId", "title", 0, 0, 50f, 15f, "", "", "")

        val STORES = (1..10).map { Store("$it", "name$it", "color$it") }

        val WATCHEES_WITH_STORES = WatcheeWithStores(WATCHEE, STORES)

        val PRICE = Price(1f, 1f, 2, "", Shop("", ""), emptySet())
    }
}