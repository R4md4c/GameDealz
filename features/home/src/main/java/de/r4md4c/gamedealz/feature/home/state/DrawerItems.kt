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

package de.r4md4c.gamedealz.feature.home.state

import androidx.annotation.DrawableRes

internal sealed class DrawerItem {

    abstract val id: Int

    abstract val text: String

    data class TextWithIcon(
        override val id: Int,
        override val text: String,
        @DrawableRes val icon: Int
    ) : DrawerItem()

    data class SwitchWithIcon(
        override val id: Int,
        override val text: String,
        @DrawableRes val icon: Int,
        val isToggled: Boolean = false
    ) : DrawerItem()

    data class SectionDivider(override val id: Int, override val text: String) : DrawerItem()

    data class TextWithDescriptionAndIcon(
        override val id: Int,
        override val text: String,
        val description: String,
        @DrawableRes val icon: Int
    ) : DrawerItem()
}
