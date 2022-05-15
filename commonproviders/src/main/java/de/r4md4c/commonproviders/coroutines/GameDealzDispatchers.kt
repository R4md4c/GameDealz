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

package de.r4md4c.commonproviders.coroutines

import de.r4md4c.gamedealz.common.IDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher

/**
 * The Main entry point for all dispatchers. Later it should be removed when Kotlin Coroutines android module becomes
 * compatible with AndroidX and Jetifier.
 *
 * You should always use this instead of the default [Dispatchers].
 */
object GameDealzDispatchers : IDispatchers {

    @OptIn(InternalCoroutinesApi::class)
    override val Main: MainCoroutineDispatcher = Dispatchers.Main

    override val IO = Dispatchers.IO

    override val Default = Dispatchers.Default
}
