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

package de.r4md4c.gamedealz.feature.detail

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.agoda.kakao.screen.Screen.Companion.onScreen
import de.r4md4c.commonproviders.date.AndroidDateFormatter
import de.r4md4c.commonproviders.res.AndroidResourcesProvider
import de.r4md4c.gamedealz.common.aware.LifecycleAware
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import de.r4md4c.gamedealz.common.state.StateVisibilityHandler
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsMviEvent
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsUIEvent
import de.r4md4c.gamedealz.test.mvi.TestMviViewModel
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class DetailsFragmentTest {

    @Mock
    lateinit var viewNotifier: ViewNotifier

    @Mock
    lateinit var navigator: Navigator

    private lateinit var testMviViewModel: TestMviViewModel<DetailsViewState, DetailsMviEvent, DetailsUIEvent>

    @Mock
    internal lateinit var lifecycleAware: LifecycleAware

    private lateinit var navController: TestNavHostController

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)
        testMviViewModel = TestMviViewModel()
    }

    @Test
    fun isAddedToWatchlist_rendersFabWithCorrectIcon_when_True() {
        launchFragment()

        testMviViewModel.emitState(createState(isAddedToWatchlist = true))

        onScreen<DetailsScreen> {
            addToWatchlistFab {
                hasDrawableWithTint(R.drawable.ic_added_to_watch_list, android.R.color.white)
            }
        }
    }

    @Test
    fun isAddedToWatchlist_rendersFabWithCorrectIcon_when_False() {
        launchFragment()

        testMviViewModel.emitState(createState(isAddedToWatchlist = false))

        onScreen<DetailsScreen> {
            addToWatchlistFab {
                hasDrawableWithTint(R.drawable.ic_add_to_watch_list, android.R.color.white)
            }
        }
    }

    @Test
    fun price_section_is_rendered_correctly() {
        val shopName = "Shop"
        launchFragment()

        createState(
            sections = listOf(
                Section.PriceSection(priceDetails = (1..10).map { Fixtures.priceDetails(shopName = shopName) })
            )
        ).also(testMviViewModel::emitState)

        onScreen<DetailsScreen> {
            recycler {
                firstChild<PriceHeaderItem> {
                    header {
                        hasText(R.string.prices)
                    }
                }
                // The rest of children
                for (i in 1 until getSize()) {
                    childAt<PriceDetailItem>(i) {
                        shop { hasText(shopName) }
                    }
                }
            }
        }
    }

    private fun launchFragment() {
        val scenario = launchFragmentInContainer(
            fragmentArgs = getArguments(),
            themeResId = R.style.BaseAppTheme,
            initialState = Lifecycle.State.INITIALIZED
        ) {
            DetailsFragment().also { fragment ->
                val context = ApplicationProvider.getApplicationContext<Context>()
                fragment.resourcesProvider =
                    AndroidResourcesProvider(context)
                fragment.dateFormatter = AndroidDateFormatter(context)
                fragment.viewNotifier = viewNotifier
                fragment.lifecycleAware = lifecycleAware
                fragment.detailsUIEventsDispatcher = testMviViewModel
                fragment.detailsMviViewModel = testMviViewModel
                fragment.stateVisibilityHandler = StateVisibilityHandler(fragment)
                fragment.navigator = navigator
                fragment.viewLifecycleOwnerLiveData.observeForever { lifecycleOwner ->
                    if (lifecycleOwner != null) {
                        // The fragment’s view has just been created
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }
        }

        scenario.onFragment { _ ->
            navController =
                TestNavHostController(ApplicationProvider.getApplicationContext()).also {
                    it.setGraph(R.navigation.nav_graph)
                }
        }

        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    private fun createState(
        isAddedToWatchlist: Boolean = false,
        sections: List<Section> = emptyList()
    ) =
        DetailsViewState(isWatched = isAddedToWatchlist, sections = sections)

    private fun getArguments(): Bundle =
        DetailsFragmentArgs(plainId = "plain", title = "title", buyUrl = "buyUrl").toBundle()
}
