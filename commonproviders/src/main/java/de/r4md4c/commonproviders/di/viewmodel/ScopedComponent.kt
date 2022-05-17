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

package de.r4md4c.commonproviders.di.viewmodel

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider

abstract class ScopedComponent : ViewModel()

@MainThread
inline fun <reified C : ScopedComponent> AppCompatActivity.components(
    noinline factory: () -> C
): Lazy<C> = ViewModelLazy(
    C::class,
    { viewModelStore }) { viewModelFactoryOf { factory() } }

@MainThread
inline fun <reified C : ScopedComponent> Fragment.components(
    noinline factory: () -> C
): Lazy<C> = ViewModelLazy(
    C::class,
    { viewModelStore }) { viewModelFactoryOf { factory() } }

@MainThread
inline fun <reified VM : ViewModel> viewModelFactoryOf(
    noinline factory: () -> VM
): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = factory() as T
    }
