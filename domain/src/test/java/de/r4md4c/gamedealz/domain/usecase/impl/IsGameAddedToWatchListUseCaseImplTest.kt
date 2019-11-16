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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.data.repository.WatchlistRepository
import de.r4md4c.gamedealz.domain.TypeParameter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class IsGameAddedToWatchListUseCaseImplTest {

    @Mock
    lateinit var watchlistRepository: WatchlistRepository

    private lateinit var subject: IsGameAddedToWatchListUseCaseImpl

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        subject = IsGameAddedToWatchListUseCaseImpl(watchlistRepository)
    }

    @Test
    fun `it should return true when findById returns non null`() {
        runBlockingTest {
            ArrangeBuilder()
                .withWatchee(mock())

            assertThat(subject.invoke(TypeParameter("")).first()).isTrue()
        }
    }

    @Test
    fun `it should return false when findById returns null`() {
        runBlocking {
            ArrangeBuilder()
                .withWatchee(null)

            assertThat(subject.invoke(TypeParameter("")).first()).isFalse()
        }
    }

    inner class ArrangeBuilder {

        fun withWatchee(watchee: Watchee?) {
            runBlocking {
                whenever(watchlistRepository.findById(any<String>())).thenReturn(flowOf(watchee))
            }
        }

    }
}