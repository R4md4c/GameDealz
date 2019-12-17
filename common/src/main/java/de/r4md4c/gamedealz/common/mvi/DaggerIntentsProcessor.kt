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

import javax.inject.Provider

abstract class DaggerIntentsProcessor<S : MviState, E : MviViewEvent>(
    private val processorsMap: Map<Class<out E>, @JvmSuppressWildcards Provider<Intent.IntentAssistedFactory<S, E>>>,
    private val modelStore: ModelStore<S>
) : IntentProcessor<E> {

    override suspend fun process(viewEvent: E) {
        processorsMap[viewEvent::class.java]?.get()?.create(viewEvent)?.let {
            modelStore.process(it)
        }
            ?: throw IllegalArgumentException("Failed to find factory for key: ${viewEvent::class.java.name}")
    }
}
