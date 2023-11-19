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
import de.r4md4c.gamedealz.domain.usecase.ToggleNightModeUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

class ToggleNightModeUseCaseImplTest {

    @Mock
    private lateinit var sharedPreferencesProvider: SharedPreferencesProvider

    @Mock
    private lateinit var appCompatProvider: AppCompatProvider

    @InjectMocks
    private lateinit var subject: ToggleNightModeUseCase

    @Before
    fun beforeEach() {
        @Suppress("DEPRECATION")
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `NightModeDisabled is set when preferences returns NightModeEnabled`() {
        whenever(sharedPreferencesProvider.activeNightMode) doReturn NightMode.Enabled

        runBlocking { subject.invoke() }

        verifyBlocking(sharedPreferencesProvider) {
            activeNightMode = NightMode.Disabled
        }
        verifyBlocking(appCompatProvider) {
            currentNightMode = NightMode.Disabled
        }
    }

    @Test
    fun `NightModeEnabled is set when preferences returns NightModeDisabled`() {
        whenever(sharedPreferencesProvider.activeNightMode) doReturn NightMode.Disabled

        runBlocking { subject.invoke() }

        verifyBlocking(sharedPreferencesProvider) {
            this.activeNightMode = NightMode.Enabled
        }
        verifyBlocking(appCompatProvider) {
            currentNightMode = NightMode.Enabled
        }
    }
}
