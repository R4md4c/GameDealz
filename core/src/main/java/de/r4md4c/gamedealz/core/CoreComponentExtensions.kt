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

package de.r4md4c.gamedealz.core

import android.app.Activity
import android.content.Context
import de.r4md4c.gamedealz.common.di.HasComponent

@Suppress("UNCHECKED_CAST")
fun Activity.coreComponent(): CoreComponent =
    (application as? HasComponent<CoreComponent>)?.daggerComponent
        ?: throw ClassCastException("Application Class has to implement HasComponent interface")

@Suppress("UNCHECKED_CAST")
fun Context.coreComponent(): CoreComponent =
    (applicationContext as? HasComponent<CoreComponent>)?.daggerComponent
        ?: throw ClassCastException("Application Class has to implement HasComponent interface")
