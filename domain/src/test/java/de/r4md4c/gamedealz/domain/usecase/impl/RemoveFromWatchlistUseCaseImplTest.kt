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
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.data.repository.WatchlistRepository
import de.r4md4c.gamedealz.domain.TypeParameter
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class RemoveFromWatchlistUseCaseImplTest {

    @Mock
    private lateinit var watchlistRepository: WatchlistRepository

    private lateinit var removeFromWatchlistUseCaseImpl: RemoveFromWatchlistUseCaseImpl

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        removeFromWatchlistUseCaseImpl = RemoveFromWatchlistUseCaseImpl(watchlistRepository)
    }


    @Test
    fun `it should return false when remove returns 0`() {
        runBlocking {
            whenever(watchlistRepository.removeById(any<String>())).thenReturn(0)

            assertThat(removeFromWatchlistUseCaseImpl.invoke(TypeParameter(""))).isFalse()
        }
    }

    @Test
    fun `it should return true when remove returns value more than 0`() {
        runBlocking {
            whenever(watchlistRepository.removeById(any<String>())).thenReturn(1)

            assertThat(removeFromWatchlistUseCaseImpl.invoke(TypeParameter(""))).isTrue()
        }
    }

    @Test
    fun `it should return false when remove returns -1`() {
        runBlocking {
            whenever(watchlistRepository.removeById(any<String>())).thenReturn(-1)

            assertThat(removeFromWatchlistUseCaseImpl.invoke(TypeParameter(""))).isFalse()
        }
    }
}
