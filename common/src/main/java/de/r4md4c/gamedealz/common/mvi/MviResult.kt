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

package de.r4md4c.gamedealz.common.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface MviResult<S : MviState>

interface ReducibleMviResult<S : MviState> : MviResult<S> {
    fun reduce(oldState: S): S
}

fun <S : MviState> emptyResult(): MviResult<S> =
    object : ReducibleMviResult<S> {
        override fun reduce(oldState: S): S = oldState
    }

fun <S : MviState> Flow<*>.ignoreResult(): Flow<MviResult<S>> =
    map { emptyResult<S>() }
