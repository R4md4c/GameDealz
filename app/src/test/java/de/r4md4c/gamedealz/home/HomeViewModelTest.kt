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
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.domain.CollectionParameter
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.CountryModel
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.usecase.*
import de.r4md4c.gamedealz.test.TestDispatchers
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class HomeViewModelTest {

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

    @Mock
    private lateinit var getAlertsCountUseCase: GetAlertsCountUseCase

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        homeViewModel = HomeViewModel(
            TestDispatchers,
            getCurrentActiveRegion,
            onActiveRegionChange,
            getStoresUseCase,
            toggleStoresUseCase,
            getAlertsCountUseCase
        )
    }

    @Test
    fun `init shows region loading indicator`() {
        ArrangeBuilder()

        homeViewModel.init()

        assertThat(homeViewModel.regionsLoading.value).isTrue()
    }

    @Test
    fun `init changes current region`() {
        val expected = activeRegion.copy()
        ArrangeBuilder()

        homeViewModel.init()

        assertThat(homeViewModel.currentRegion.value).isEqualTo(expected)
    }

    @Test
    fun `init should invoke failure handler when current region fails to be retrieved`() {
        ArrangeBuilder()
            .withFailedCurrentRegion()

        homeViewModel.init()

        homeViewModel.onError.test().assertHasValue()
        assertThat(homeViewModel.currentRegion.value).isNull()
    }

    @Test
    fun `init starts listening to stores when current region is success`() {
        ArrangeBuilder()

        homeViewModel.init()

        runBlocking {
            verify(getStoresUseCase).invoke(TypeParameter(activeRegion))
        }
    }

    @Test
    fun `init starts listening to stores and posts to live data when current is success`() {
        ArrangeBuilder()
            .withStore(emptyList())

        homeViewModel.init()

        assertThat(homeViewModel.stores.value).isNotNull()
    }

    @Test
    fun `init starts listening to stores and posts to onError live data when get stores usecase fails`() {
        ArrangeBuilder()
            .withFailedGetStores()

        val errorTS = homeViewModel.onError.test()
        homeViewModel.init()

        errorTS.assertHasValue()
    }

    @Test
    fun `onStoreSelected invokes toggle stores usecase`() {
        val storeModel = StoreModel("", "", true)
        ArrangeBuilder()

        homeViewModel.onStoreSelected(storeModel)

        runBlocking {
            verify(toggleStoresUseCase).invoke(CollectionParameter(setOf(storeModel)))
        }
    }

    @Test
    fun `onStoreSelected posts to onError Live data when toggle stores fails`() {
        val storeModel = StoreModel("", "", true)
        ArrangeBuilder()
            .withFailedToggleStores()

        homeViewModel.onStoreSelected(storeModel)

        assertThat(homeViewModel.onError.value).isNotNull()
    }

    @Test
    fun `onNavigateTo calls navigator`() {
        val mockedNavigator = mock<Navigator>()

        homeViewModel.onNavigateTo(mockedNavigator, "")

        verify(mockedNavigator).navigate("")
    }

    @Test
    fun `closeDrawer posts to closeDrawer live data`() {
        homeViewModel.closeDrawer()

        assertThat(homeViewModel.closeDrawer).isNotNull()
    }

    @Test
    fun `onRegionChangeClicked opens region selection dialog`() {
        ArrangeBuilder()

        homeViewModel.onRegionChangeClicked()

        assertThat(homeViewModel.openRegionSelectionDialog.value).isEqualTo(activeRegion)
    }

    @Test
    fun `onRegionChangeClicked posts to onError Live Data when getCurrentActiveRegion fails`() {
        ArrangeBuilder()
            .withFailedCurrentRegion()

        homeViewModel.onRegionChangeClicked()

        assertThat(homeViewModel.openRegionSelectionDialog.value).isNull()
        assertThat(homeViewModel.onError.value).isNotNull()
    }

    private val activeRegion = ActiveRegion("", CountryModel(""), CurrencyModel("", ""))

    private inner class ArrangeBuilder {
        init {
            runBlocking {
                whenever(getCurrentActiveRegion.invoke(anyOrNull())).thenReturn(activeRegion)
                whenever(onActiveRegionChange.activeRegionChange()).thenReturn(produce { this.close() })
                whenever(getStoresUseCase.invoke(anyOrNull())).thenReturn(produce { this.close() })
                whenever(toggleStoresUseCase.invoke(anyOrNull())).thenReturn(Unit)
            }
        }

        fun withFailedCurrentRegion() = apply {
            runBlocking {
                whenever(getCurrentActiveRegion.invoke(anyOrNull())).thenThrow(RuntimeException(""))
            }
        }

        fun withStore(stores: List<StoreModel>) = apply {
            runBlocking {
                whenever(getStoresUseCase.invoke(anyOrNull())).thenReturn(produce(capacity = 1) { send(stores) })
            }
        }

        fun withFailedGetStores() = apply {
            runBlocking {
                whenever(getStoresUseCase.invoke(anyOrNull())).thenThrow(RuntimeException(""))
            }
        }

        fun withFailedToggleStores() = apply {
            runBlocking {
                whenever(toggleStoresUseCase.invoke(anyOrNull())).thenThrow(RuntimeException(""))
            }
        }
    }
}