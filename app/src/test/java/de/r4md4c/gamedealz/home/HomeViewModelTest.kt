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

package de.r4md4c.gamedealz.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import de.r4md4c.commonproviders.coroutines.IDispatchers
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.domain.usecase.ToggleStoresUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class HomeViewModelTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var homeViewModel: HomeViewModel

    @Mock
    private lateinit var getCurrentActiveRegion: GetCurrentActiveRegionUseCase

    @Mock
    private lateinit var onActiveRegionChange: OnCurrentActiveRegionReactiveUseCase

    @Mock
    private lateinit var getStoresUseCase: GetStoresUseCase

    @Mock
    private lateinit var toggleStoresUseCase: ToggleStoresUseCase

    @Before
    fun beforeEach() {
        Dispatchers.setMain(mainThreadSurrogate)
        MockitoAnnotations.initMocks(this)

        homeViewModel = HomeViewModel(object : IDispatchers {
            override val Main: CoroutineDispatcher
                get() = Dispatchers.Main
            override val IO: CoroutineDispatcher
                get() = Dispatchers.IO
            override val Default: CoroutineDispatcher
                get() = Dispatchers.Default
        }, getCurrentActiveRegion, onActiveRegionChange, getStoresUseCase, toggleStoresUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun `hello`() {
        homeViewModel.init()
    }

}