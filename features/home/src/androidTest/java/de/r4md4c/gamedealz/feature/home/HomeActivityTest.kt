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

package de.r4md4c.gamedealz.feature.home

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import de.r4md4c.gamedealz.auth.AuthActivityDelegate
import de.r4md4c.gamedealz.common.mvi.MviViewModel
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.CountryModel
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent
import de.r4md4c.gamedealz.feature.home.state.HomeUserStatus
import de.r4md4c.gamedealz.feature.home.state.HomeViewState
import de.r4md4c.gamedealz.feature.home.state.PriceAlertCount
import de.r4md4c.gamedealz.test.scenario.InjectableActivityScenario
import de.r4md4c.gamedealz.test.scenario.injectableActivityScenario
import de.r4md4c.gamedealz.test.utils.Page.Companion.on
import de.r4md4c.gamedealz.test.utils.createFragmentFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

class HomeActivityTest {

    companion object {
        @ClassRule
        @JvmField
        val disableAnimationsRule = DisableAnimationsRule()
    }

    private val injectorScenario =
        injectableActivityScenario<HomeActivity> {
            injectActivity {
                setTheme(R.style.BaseAppTheme)
                supportFragmentManager.fragmentFactory = createFragmentFactory {
                    when (it) {
                        NavHostFragment::class.java.name -> NavHostFragment.create(R.navigation.nav_graph)
                        else -> Fragment()
                    }
                }
                viewModel = mockViewModel
                viewNotifier = mockViewNotifier
                authDelegate = mockAuthDelegate
            }
        }

    @Mock
    private lateinit var mockViewNotifier: ViewNotifier

    @Mock
    private lateinit var mockAuthDelegate: AuthActivityDelegate

    @Mock
    private lateinit var mockViewModel: MviViewModel<HomeViewState, HomeMviViewEvent>

    private val eventsCollections = mutableListOf<HomeMviViewEvent>()

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun initialState_rendersCorrectly(): Unit = injectorScenario.use {
        ArrangeBuilder(it)
            .withState(HomeViewState())
            .arrange()

        on<SideMenu>()
            .verifyLoggedOut()
    }

    @Test
    fun activeRegion_rendersCorrectly(): Unit = injectorScenario.use {
        ArrangeBuilder(it)
            .withState(
                HomeViewState(
                    activeRegion = ActiveRegion(
                        "US",
                        CountryModel("US"),
                        CurrencyModel("USD", "$")
                    )
                )
            )
            .arrange()

        on<SideMenu>()
            .verifyCountry("United States")
    }

    @Test
    fun userStatusLoggedIn_rendersCorrectly_whenKnownUser(): Unit = injectorScenario.use {
        ArrangeBuilder(it)
            .withState(
                HomeViewState(
                    homeUserStatus = HomeUserStatus.LoggedIn.KnownUser("R4md4c")
                )
            )
            .arrange()

        on<SideMenu>()
            .verifyKnownUser("R4md4c")
    }

    @Test
    fun userStatusLoggedIn_rendersCorrectly_whenUnKnownUser(): Unit = injectorScenario.use {
        ArrangeBuilder(it)
            .withState(
                HomeViewState(
                    homeUserStatus = HomeUserStatus.LoggedIn.UnknownUser
                )
            )
            .arrange()

        on<SideMenu>()
            .verifyUnknownUser()
    }

    @Test
    fun clickingLogout_invokesLogoutEvent(): Unit = injectorScenario.use {
        ArrangeBuilder(it)
            .withState(
                HomeViewState(
                    homeUserStatus = HomeUserStatus.LoggedIn.UnknownUser
                )
            )
            .arrange()

        on<SideMenu>()
            .clickAccountPanel()
            .clickLogout()

        assertThat(eventsCollections.last()).isEqualTo(HomeMviViewEvent.LogoutViewEvent)
    }

    @Test
    fun priceAlertCount_renderedCorrectly(): Unit = injectorScenario.use {
        ArrangeBuilder(it)
            .withState(
                HomeViewState(
                    priceAlertsCount = PriceAlertCount.Set(20)
                )
            )
            .arrange()

        on<SideMenu>()
            .verifyPriceAlertCount(20)
    }

    private inner class ArrangeBuilder(
        private val scenario: InjectableActivityScenario<HomeActivity>
    ) {

        init {
            whenever(mockViewModel.onViewEvents(any(), any())).doAnswer { invocationOnMock ->
                val flow = invocationOnMock.getArgument<Flow<HomeMviViewEvent>>(0)
                val scope = invocationOnMock.getArgument<CoroutineScope>(1)

                val job = scope.launch {
                    flow.collect { eventsCollections += it }
                }
                job.invokeOnCompletion { eventsCollections.clear() }
                Unit
            }
        }

        fun withState(state: HomeViewState) = apply {
            whenever(mockViewModel.modelState).doReturn(flowOf(state))
        }

        fun arrange() {
            scenario.launch()
        }
    }
}
