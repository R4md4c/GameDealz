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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

@UseExperimental(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.recordWith(recorder: FlowRecorder<T>): Job =
    onEach { recorder.values.addLast(it) }.launchIn(recorder.coroutineScope)

class FlowRecorder<T>(internal val coroutineScope: CoroutineScope) : Iterable<T> {

    internal val values: BlockingDeque<T> = LinkedBlockingDeque<T>()

    fun takeValue(): T {
        return values.pollFirst(1, TimeUnit.SECONDS)
            ?: throw NoSuchElementException("No value found.")
    }

    override fun iterator(): Iterator<T> = values.iterator()

    fun clearValues() {
        values.clear()
    }

    @Suppress("UseCheckOrError")
    fun assertNoMoreValues() {
        try {
            val value = takeValue()
            throw IllegalStateException("Expected no more values but got $value")
        } catch (ignored: NoSuchElementException) {
        }
    }
}
