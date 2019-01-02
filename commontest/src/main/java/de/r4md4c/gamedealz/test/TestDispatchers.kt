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

package de.r4md4c.gamedealz.test

import de.r4md4c.gamedealz.common.IDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * A Singleton that will be used primarily during JUnit tests.
 * It resorts back to using the original Coroutines Main dispatcher instead of the default Android
 */
object TestDispatchers : IDispatchers {

    override val Main: CoroutineDispatcher
        get() = Dispatchers.Unconfined

    override val IO: CoroutineDispatcher
        get() = Dispatchers.Unconfined

    override val Default: CoroutineDispatcher
        get() = Dispatchers.Unconfined
}
