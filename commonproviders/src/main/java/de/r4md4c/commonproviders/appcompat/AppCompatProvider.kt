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

package de.r4md4c.commonproviders.appcompat

import androidx.appcompat.app.AppCompatDelegate

sealed class NightMode(val name: String) {
    object Enabled : NightMode("night_mode_on")
    object Disabled : NightMode("night_mode_off")


    companion object {
        fun fromString(name: String): NightMode =
            when (name) {
                Enabled.name -> Enabled
                Disabled.name -> Disabled
                else -> throw IllegalArgumentException("$name is unknown")
            }

        @AppCompatDelegate.NightMode
        fun toAppCompatNightMode(nightMode: NightMode): Int =
            when (nightMode) {
                is NightMode.Enabled -> AppCompatDelegate.MODE_NIGHT_YES
                is NightMode.Disabled -> AppCompatDelegate.MODE_NIGHT_NO
            }
    }
}

interface AppCompatProvider {

    var currentNightMode: NightMode

}
