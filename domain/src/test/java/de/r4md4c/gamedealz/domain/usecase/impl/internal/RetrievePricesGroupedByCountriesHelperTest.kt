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

import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.network.model.PriceDTO
import de.r4md4c.gamedealz.network.repository.PricesRemoteDataSource
import kotlinx.coroutines.runBlocking
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

class RetrievePricesGroupedByCountriesHelperTest {

    @Mock
    lateinit var pricesRemoteDataSource: PricesRemoteDataSource

    private lateinit var subject: RetrievePricesGroupedByCountriesHelper

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        subject = RetrievePricesGroupedByCountriesHelper(pricesRemoteDataSource)
    }

    @Test
    fun `it retrieves prices several times with different country and region ids`() {
        runBlocking {
            ArrangeBuilder()

            subject.prices(
                listOf(
                    WATCHEE.copy(regionCode = "US", countryCode = "us"),
                    WATCHEE.copy(plainId = "plainId2", regionCode = "US", countryCode = "us"),
                    WATCHEE.copy(regionCode = "US", countryCode = "CA"),
                    WATCHEE.copy(regionCode = "EU", countryCode = "DE"),
                    WATCHEE.copy(regionCode = "EU", countryCode = "UK")
                )
            )

            verify(pricesRemoteDataSource).retrievesPrices(
                eq(setOf("plainId", "plainId2")),
                any(),
                eq("US"),
                eq("us"),
                any()
            )
            verify(pricesRemoteDataSource).retrievesPrices(any(), any(), eq("US"), eq("CA"), any())
            verify(pricesRemoteDataSource).retrievesPrices(any(), any(), eq("EU"), eq("DE"), any())
            verify(pricesRemoteDataSource).retrievesPrices(any(), any(), eq("EU"), eq("UK"), any())
        }
    }


    @Test
    fun `it retrieves prices with maximal added from lastCheckedTimestamp`() {
        runBlocking {
            ArrangeBuilder()

            subject.prices(
                listOf(
                    WATCHEE.copy(lastCheckDate = 1), WATCHEE.copy(lastCheckDate = 2),
                    WATCHEE.copy(lastCheckDate = 3), WATCHEE.copy(lastCheckDate = 4)
                )
            )

            verify(pricesRemoteDataSource).retrievesPrices(
                eq(setOf("plainId")),
                any(),
                any(),
                any(),
                eq(4)
            )
        }
    }

    @Test
    fun `it retrieves prices with maximal added from addedTimestamp if lastCheckDate was 0`() {
        runBlocking {
            ArrangeBuilder()

            subject.prices(
                listOf(
                    WATCHEE.copy(lastCheckDate = 1), WATCHEE.copy(dateAdded = 10),
                    WATCHEE.copy(lastCheckDate = 3), WATCHEE.copy(lastCheckDate = 4)
                )
            )

            verify(pricesRemoteDataSource).retrievesPrices(
                eq(setOf("plainId")),
                any(),
                any(),
                any(),
                eq(10)
            )
        }
    }

    @Test
    fun `it returns prices from priceRemoteRepository`() {
        runBlocking {
            val expected: Map<String, List<PriceDTO>> =
                mapOf("planId" to listOf(mock(), mock(), mock()))
            ArrangeBuilder()
                .withReturnedPrices(expected)

            val result = subject.prices(
                listOf(
                    WATCHEE.copy(lastCheckDate = 1), WATCHEE.copy(dateAdded = 10),
                    WATCHEE.copy(lastCheckDate = 3), WATCHEE.copy(lastCheckDate = 4)
                )
            )

            assertThat(result).isEqualTo(expected)
        }
    }

    inner class ArrangeBuilder {
        init {
            runBlocking {
                whenever(
                    pricesRemoteDataSource.retrievesPrices(
                        any(),
                        any(),
                        anyOrNull(),
                        anyOrNull(),
                        anyOrNull()
                    )
                )
                    .thenReturn(emptyMap())
            }
        }

        fun withReturnedPrices(map: Map<String, List<PriceDTO>>) {
            runBlocking {
                whenever(
                    pricesRemoteDataSource.retrievesPrices(
                        any(),
                        any(),
                        anyOrNull(),
                        anyOrNull(),
                        anyOrNull()
                    )
                )
                    .thenReturn(map)
            }
        }
    }

    companion object {
        private val WATCHEE = Watchee(
            1, "plainId", "", 0L, 0L, 0f, "",
            0f, "", "", ""
        )
    }
}