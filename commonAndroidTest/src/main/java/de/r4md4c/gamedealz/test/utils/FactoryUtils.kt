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

package de.r4md4c.gamedealz.test.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Creates a dummy [FragmentFactory] that invokes the [factory] lambda to return Fragments.
 */
inline fun createFragmentFactory(crossinline factory: (String) -> Fragment): FragmentFactory =
    object : FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
            factory(className)
    }

@Suppress("UNCHECKED_CAST")
inline fun createViewModelFactory(
    crossinline factory: (Class<out ViewModel>) -> ViewModel
): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = factory(modelClass) as T
    }
