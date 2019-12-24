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

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.navigation.ui.NavigationUI
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SectionDrawerItem
import com.mikepenz.materialdrawer.model.SwitchDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import de.r4md4c.gamedealz.auth.AuthActivityDelegate
import de.r4md4c.gamedealz.common.aware.DrawerAware
import de.r4md4c.gamedealz.common.base.HasDrawerLayout
import de.r4md4c.gamedealz.common.coroutines.lifecycleLog
import de.r4md4c.gamedealz.common.mvi.UiSideEffect
import de.r4md4c.gamedealz.common.mvi.ViewEventFlow
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import de.r4md4c.gamedealz.core.coreComponent
import de.r4md4c.gamedealz.domain.model.displayName
import de.r4md4c.gamedealz.feature.home.di.DaggerHomeComponent
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent.InitViewEvent
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent.NightModeToggleViewEvent
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.feature.home.state.HomeUiSideEffect
import de.r4md4c.gamedealz.feature.home.state.HomeUserStatus
import de.r4md4c.gamedealz.feature.home.state.HomeUserStatus.LoggedOut
import de.r4md4c.gamedealz.feature.home.state.PriceAlertCount
import de.r4md4c.gamedealz.feature.region.RegionSelectionDialogFragmentArgs
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooManyFunctions")
internal class HomeActivity : AppCompatActivity(), DrawerAware, HasDrawerLayout,
    ViewEventFlow<HomeMviViewEvent> {

    private lateinit var drawer: Drawer

    private val viewEventsChannel = Channel<HomeMviViewEvent>()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var authDelegate: AuthActivityDelegate

    @Inject
    lateinit var viewNotifier: ViewNotifier

    private val viewModel by viewModels<HomeViewModel> { viewModelFactory }

    private val navController
        get() = findNavController(R.id.nav_host_fragment)

    private val accountHeader by lazy {
        AccountHeaderBuilder()
            .withActivity(this)
            .withCompactStyle(true)
            .withProfileImagesVisible(false)
            .withSelectionListEnabled(true)
            .withCurrentProfileHiddenInList(true)
            .withOnAccountHeaderSelectionViewClickListener { _, profile ->
                if (profile == null) {
                    onLoginClick()
                    true
                } else {
                    false
                }
            }
            // This will be called when user click the logout item in the expandable
            // list below the profile.
            .withOnAccountHeaderListener { _, profile, _ ->
                if (profile.identifier == R.id.home_drawer_account_logout.toLong()) {
                    onLogoutClick()
                    true
                } else {
                    false
                }
            }
            .build()
    }

    override val drawerLayout: DrawerLayout
        get() = drawer.drawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        onInject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadDrawer(savedInstanceState)
        insertMenuItems()
        listenForDestinationChanges()

        viewModel.modelState
            .lifecycleLog(name = "HomeActivity")
            .onEach { render(it) }
            .launchIn(lifecycleScope)

        viewModel.onViewEvents(
            lifecycleScope,
            viewEvents().onStart { emit(InitViewEvent(lifecycleScope)) })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        drawer.saveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authDelegate.onActivityResult(this, requestCode, data)
    }

    override fun onSupportNavigateUp(): Boolean =
        NavigationUI.navigateUp(navController, drawer.drawerLayout)

    override fun closeDrawer() {
        drawer.closeDrawer()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
            return
        }
        super.onBackPressed()
    }

    private fun render(state: HomeMviViewState) {
        renderPriceAlertCount(state)
        renderRegion(state)

        findDrawerItem<SwitchDrawerItem>(R.id.home_drawer_night_mode_switch)
            ?.withChecked(state.nightModeEnabled)

        drawer.adapter.notifyAdapterDataSetChanged()

        state.renderUserStatus()
        state.uiSideEffect?.let { renderSideEffects(it) }
    }

    override fun viewEvents(): Flow<HomeMviViewEvent> = viewEventsChannel.consumeAsFlow()

    private fun insertMenuItems() {
        val dealsDrawerItem = PrimaryDrawerItem()
            .withName(R.string.title_on_going_deals)
            .withIdentifier(R.id.dealsFragment.toLong())
            .withIcon(R.drawable.ic_deal)
            .withIconTintingEnabled(true)
            .withOnDrawerItemClickListener { _, _, _ ->
                viewEventsChannel.offer(HomeMviViewEvent.NavigateToOngoingDealsScreen)
                false
            }

        val managedWatchlistDrawerItem = PrimaryDrawerItem()
            .withName(R.string.title_manage_your_watchlist)
            .withIdentifier(R.id.manageWatchlistFragment.toLong())
            .withIcon(R.drawable.ic_add_to_watch_list)
            .withIconTintingEnabled(true)
            .withOnDrawerItemClickListener { _, _, _ ->
                viewEventsChannel.offer(HomeMviViewEvent.NavigateToManageWatchlistScreen)
                false
            }

        val nightModeDrawerItem = SwitchDrawerItem()
            .withName(R.string.enable_night_mode)
            .withIcon(R.drawable.ic_weather_night)
            .withSelectable(false)
            .withIconTintingEnabled(true)
            .withIdentifier(R.id.home_drawer_night_mode_switch.toLong())
            .withOnCheckedChangeListener { _, _, _ ->
                viewEventsChannel.offer(NightModeToggleViewEvent(lifecycleScope))
            }

        val section = SectionDrawerItem()
            .withDivider(true)
            .withName(R.string.miscellaneous)

        val secondary = PrimaryDrawerItem()
            .withIdentifier(R.id.home_drawer_region_selection.toLong())
            .withName(R.string.change_region)
            .withIcon(R.drawable.ic_region)
            .withIconTintingEnabled(true)
            .withSelectable(false)
            .withOnDrawerItemClickListener { _, _, _ ->
                onRegionSelectionClick()
                true
            }

        drawer.setItems(
            listOf(
                dealsDrawerItem,
                managedWatchlistDrawerItem,
                section,
                secondary,
                nightModeDrawerItem
            )
        )
    }

    private fun loadDrawer(savedInstanceState: Bundle?) {
        drawer = DrawerBuilder(this)
            .withAccountHeader(accountHeader)
            .withCloseOnClick(true)
            .withHasStableIds(true)
            .apply { savedInstanceState?.let { withSavedInstance(it) } }
            .build()
    }

    private fun onRegionSelectionClick() = lifecycleScope.launch {
        viewModel.modelState.first().activeRegion?.let {
            navController.navigate(
                R.id.regionSelectionDialog,
                RegionSelectionDialogFragmentArgs(it).toBundle()
            )
        }
    }

    private fun listenForDestinationChanges() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (drawer.currentSelectedPosition == -1 || drawer.currentSelection != destination.id.toLong()) {
                drawer.setSelection(destination.id.toLong(), false)
            }
        }
    }

    private fun navigateToDestination(@IdRes identifier: Int, popUpToRoot: Boolean) {
        if (identifier == navController.currentDestination?.id) return
        val navOptionsBuilder = navOptions {
            if (identifier == R.id.dealsFragment) {
                launchSingleTop = true
            }
            anim {
                popEnter = R.anim.nav_default_pop_enter_anim
                popExit = R.anim.nav_default_pop_exit_anim
                enter = R.anim.nav_default_enter_anim
                exit = R.anim.nav_default_exit_anim
            }
            if (popUpToRoot) {
                popUpTo(identifier) { inclusive = false }
            }
        }
        navController.navigate(identifier, null, navOptionsBuilder)
    }

    private fun HomeMviViewState.renderUserStatus() = with(accountHeader) {
        clear()
        return@with when (homeUserStatus) {
            is LoggedOut -> {
                setSelectionFirstLine(getString(R.string.sign_in))
                setSelectionSecondLine(getString(R.string.sign_in_isthereanydeal))
            }
            is HomeUserStatus.LoggedIn -> {
                setSelectionSecondLine(EMPTY_STRING)
                val userLabel = when (homeUserStatus) {
                    is HomeUserStatus.LoggedIn.KnownUser -> homeUserStatus.username
                    is HomeUserStatus.LoggedIn.UnknownUser -> getString(R.string.signed_in)
                }

                setSelectionFirstLine(userLabel)

                ProfileDrawerItem().withName(userLabel)
                    .withIdentifier(R.id.home_drawer_account_main_profile.toLong())
                    .also {
                        addProfile(it, 0)
                    }

                addProfile(
                    ProfileDrawerItem().withName(R.string.log_out)
                        .withIdentifier(R.id.home_drawer_account_logout.toLong()), 1
                )
            }
        }
    }

    private fun renderRegion(state: HomeMviViewState) {
        state.activeRegion?.let {
            findDrawerItem<PrimaryDrawerItem>(R.id.home_drawer_region_selection)
                ?.withDescription(it.country.displayName())
        }
    }

    private fun renderPriceAlertCount(
        state: HomeMviViewState
    ) {
        val countString = when (state.priceAlertsCount) {
            is PriceAlertCount.NotSet -> EMPTY_STRING
            is PriceAlertCount.Set -> state.priceAlertsCount.count.toString()
        }
        findDrawerItem<PrimaryDrawerItem>(R.id.manageWatchlistFragment)?.withBadge(countString)
    }

    private fun renderSideEffects(effects: UiSideEffect<HomeUiSideEffect>) = effects.take {
        when (it) {
            is HomeUiSideEffect.ShowAuthenticationError ->
                it.message?.let { message -> viewNotifier.notify(message) }
            is HomeUiSideEffect.NotifyUserHasLoggedOut -> viewNotifier.notify(getString(R.string.signed_out))
            is HomeUiSideEffect.NotifyUserHasLoggedIn -> viewNotifier.notify(
                it.username?.let { name -> getString(R.string.welcome_user, name) }
                    ?: getString(R.string.welcome_user_unknown)
            )
            is HomeUiSideEffect.StartAuthenticationFlow -> authDelegate.startAuthFlow(this)
            is HomeUiSideEffect.NavigateSideEffect -> navigateToDestination(
                it.navigationIdentifier,
                it.popToRoot
            )
        } as Any
    }

    private fun onLogoutClick() {
        viewEventsChannel.offer(HomeMviViewEvent.LogoutViewEvent)
    }

    private fun onLoginClick() {
        viewEventsChannel.offer(HomeMviViewEvent.LoginViewEvent)
    }

    private inline fun <reified T : IDrawerItem<*, *>> findDrawerItem(@IdRes identifier: Int) =
        drawer.getDrawerItem(identifier.toLong()) as? T

    private fun onInject() {
        coreComponent().also {
            DaggerHomeComponent.factory()
                .create(this, it, it.authComponent)
                .inject(this)
        }
    }
}

private const val EMPTY_STRING = ""
