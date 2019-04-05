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

import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class OnNightModeChangeUseCaseImplTest {

    @MockK
    private lateinit var sharedPreferencesProvider: SharedPreferencesProvider

    @InjectMockKs
    private lateinit var subject: OnNightModeChangeUseCaseImpl

    @Before
    fun beforeEach() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `it should invoke sharedPreferencesProvider`() {
        coEvery { sharedPreferencesProvider.reactiveNightMode } returns Channel()

        runBlocking { subject.activeNightModeChange() }

        verify { sharedPreferencesProvider.reactiveNightMode }
    }
}
