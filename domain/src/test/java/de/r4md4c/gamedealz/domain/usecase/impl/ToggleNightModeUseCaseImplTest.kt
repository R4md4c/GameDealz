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

import de.r4md4c.commonproviders.appcompat.AppCompatProvider
import de.r4md4c.commonproviders.appcompat.NightMode
import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ToggleNightModeUseCaseImplTest {

    @MockK(relaxed = true)
    private lateinit var sharedPreferencesProvider: SharedPreferencesProvider

    @MockK(relaxed = true)
    private lateinit var appCompatProvider: AppCompatProvider

    @InjectMockKs
    private lateinit var subject: ToggleNightModeUseCaseImpl

    @Before
    fun beforeEach() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `NightModeDisabled is set when preferences returns NightModeEnabled`() {
        coEvery { sharedPreferencesProvider.activeNightMode } returns NightMode.Enabled

        runBlocking { subject.invoke() }

        coVerify {
            sharedPreferencesProvider.activeNightMode = NightMode.Disabled
            appCompatProvider.currentNightMode = NightMode.Disabled
        }
    }

    @Test
    fun `NightModeEnabled is set when preferences returns NightModeDisabled`() {
        coEvery { sharedPreferencesProvider.activeNightMode } returns NightMode.Disabled

        runBlocking { subject.invoke() }

        coVerify {
            sharedPreferencesProvider.activeNightMode = NightMode.Enabled
            appCompatProvider.currentNightMode = NightMode.Enabled
        }
    }

}
