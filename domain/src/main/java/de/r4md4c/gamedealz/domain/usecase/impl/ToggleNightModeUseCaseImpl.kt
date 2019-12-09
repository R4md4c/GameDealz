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
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.usecase.ToggleNightModeUseCase
import javax.inject.Inject

internal class ToggleNightModeUseCaseImpl @Inject constructor(
    private val sharedPreferencesProvider: SharedPreferencesProvider,
    private val appCompatProvider: AppCompatProvider
) : ToggleNightModeUseCase {

    override suspend fun invoke(param: VoidParameter?) {
        val toggledMode = when (sharedPreferencesProvider.activeNightMode) {
            is NightMode.Enabled -> NightMode.Disabled
            is NightMode.Disabled -> NightMode.Enabled
        }

        appCompatProvider.currentNightMode = toggledMode
        sharedPreferencesProvider.activeNightMode = toggledMode
    }
}
