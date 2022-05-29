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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
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
import de.r4md4c.commonproviders.di.viewmodel.viewModelFactoryOf
import de.r4md4c.gamedealz.auth.AuthActivityDelegate
import de.r4md4c.gamedealz.common.aware.DrawerAware
import de.r4md4c.gamedealz.common.base.HasDrawerLayout
import de.r4md4c.gamedealz.common.exhaustive
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import de.r4md4c.gamedealz.core.coreComponent
import de.r4md4c.gamedealz.domain.model.displayName
import de.r4md4c.gamedealz.feature.home.databinding.ActivityMainBinding
import de.r4md4c.gamedealz.feature.home.di.DaggerHomeComponent
import de.r4md4c.gamedealz.feature.home.state.HomeUserStatus
import de.r4md4c.gamedealz.feature.home.state.HomeUserStatus.LoggedOut
import de.r4md4c.gamedealz.feature.home.state.HomeViewState
import de.r4md4c.gamedealz.feature.home.state.PriceAlertCount
import de.r4md4c.gamedealz.feature.home.state.RegionStatus
import de.r4md4c.gamedealz.feature.region.RegionSelectionDialogFragmentArgs
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Provider

@Suppress("TooManyFunctions")
internal class HomeActivity : AppCompatActivity(), DrawerAware, HasDrawerLayout {

    private lateinit var drawer: Drawer

    @Inject
    lateinit var authDelegate: AuthActivityDelegate

    @Inject
    lateinit var viewNotifier: ViewNotifier

    @Inject
    lateinit var viewModelProvider: Provider<HomeViewModel>

    private val viewModel by viewModels<HomeViewModel> {
        viewModelFactoryOf { viewModelProvider.get() }
    }

    private val navController: NavController
        get() {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            return navHostFragment.navController
        }

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
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadDrawer(savedInstanceState)
        insertMenuItems()
        listenForDestinationChanges()

        viewModel.state.onEach {
            render(it)
        }.launchIn(lifecycleScope)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        drawer.saveInstanceState(outState)
    }

    @Suppress("DEPRECATION")
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

    private fun render(state: HomeViewState) {
        renderPriceAlertCount(state)
        renderRegion(state)

        findDrawerItem<SwitchDrawerItem>(R.id.home_drawer_night_mode_switch)
            ?.withChecked(state.nightModeEnabled)

        drawer.adapter.notifyAdapterDataSetChanged()

        state.renderUserStatus()
        state.uiMessage?.let(this::renderUiMessage)
    }

    private fun insertMenuItems() {
        val dealsDrawerItem = PrimaryDrawerItem()
            .withName(R.string.title_on_going_deals)
            .withIdentifier(R.id.dealsFragment.toLong())
            .withIcon(R.drawable.ic_deal)
            .withIconTintingEnabled(true)
            .withOnDrawerItemClickListener { _, _, _ ->
                navigateToDestination(
                    R.id.dealsFragment,
                    popUpToRoot = true
                )
                false
            }

        val managedWatchlistDrawerItem = PrimaryDrawerItem()
            .withName(R.string.title_manage_your_watchlist)
            .withIdentifier(R.id.manageWatchlistFragment.toLong())
            .withIcon(R.drawable.ic_add_to_watch_list)
            .withIconTintingEnabled(true)
            .withOnDrawerItemClickListener { _, _, _ ->
                navigateToDestination(R.id.manageWatchlistFragment, popUpToRoot = false)
                false
            }

        val nightModeDrawerItem = SwitchDrawerItem()
            .withName(R.string.enable_night_mode)
            .withIcon(R.drawable.ic_weather_night)
            .withSelectable(false)
            .withIconTintingEnabled(true)
            .withIdentifier(R.id.home_drawer_night_mode_switch.toLong())
            .withOnCheckedChangeListener { _, _, _ ->
                viewModel.onToggleNightMode()
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

    private fun onRegionSelectionClick() {
        val activeRegion = viewModel.state.value.regionStatus as? RegionStatus.Active
        if (activeRegion != null) {
            navController.navigate(
                R.id.regionSelectionDialog,
                RegionSelectionDialogFragmentArgs(activeRegion.region).toBundle()
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

    private fun HomeViewState.renderUserStatus() = with(accountHeader) {
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

    private fun renderRegion(state: HomeViewState) {
        val activeRegion = (state.regionStatus as? RegionStatus.Active)?.region
        if (activeRegion != null) {
            findDrawerItem<PrimaryDrawerItem>(R.id.home_drawer_region_selection)
                ?.withDescription(activeRegion.country.displayName())
        }
    }

    private fun renderPriceAlertCount(
        state: HomeViewState
    ) {
        val countString = when (state.priceAlertsCount) {
            is PriceAlertCount.NotSet -> EMPTY_STRING
            is PriceAlertCount.Set -> state.priceAlertsCount.count.toString()
        }
        findDrawerItem<PrimaryDrawerItem>(R.id.manageWatchlistFragment)?.withBadge(countString)
    }

    private fun renderUiMessage(message: HomeUIMessage) {
        when (message) {
            is HomeUIMessage.ShowAuthenticationError ->
                viewNotifier.notify(message.reason)
            is HomeUIMessage.NotifyUserHasLoggedIn -> viewNotifier.notify(
                message.username?.let { name -> getString(R.string.welcome_user, name) }
                    ?: getString(R.string.welcome_user_unknown)
            )
            HomeUIMessage.NotifyUserHasLoggedOut -> viewNotifier.notify(getString(R.string.signed_out))
        }.exhaustive
        viewModel.clearMessage(message)
    }

    private fun onLogoutClick() {
        viewModel.onLogout()
    }

    private fun onLoginClick() {
        authDelegate.startAuthFlow(this)
    }

    private inline fun <reified T : IDrawerItem<*, *>> findDrawerItem(@IdRes identifier: Int) =
        drawer.getDrawerItem(identifier.toLong()) as? T

    private fun onInject() {
        coreComponent().also { coreComponent ->
            DaggerHomeComponent.factory()
                .create(this, coreComponent, coreComponent.authComponent)
                .inject(this)
        }
    }
}

private const val EMPTY_STRING = ""
