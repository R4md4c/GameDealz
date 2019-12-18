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
import com.mikepenz.materialdrawer.model.SectionDrawerItem
import com.mikepenz.materialdrawer.model.SwitchDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import de.r4md4c.gamedealz.common.aware.DrawerAware
import de.r4md4c.gamedealz.common.base.HasDrawerLayout
import de.r4md4c.gamedealz.common.coroutines.lifecycleLog
import de.r4md4c.gamedealz.common.mvi.ViewEventFlow
import de.r4md4c.gamedealz.core.coreComponent
import de.r4md4c.gamedealz.domain.model.displayName
import de.r4md4c.gamedealz.feature.home.di.DaggerHomeComponent
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent.InitViewEvent
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent.NightModeToggleViewEvent
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
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

    private val viewModel by viewModels<HomeViewModel> { viewModelFactory }

    private val navController
        get() = findNavController(R.id.nav_host_fragment)

    private val accountHeader by lazy {
        AccountHeaderBuilder()
            .withActivity(this)
            .withSelectionFirstLine(getString(R.string.sign_in))
            .withSelectionSecondLine(getString(R.string.click_to_signin))
            .withOnAccountHeaderSelectionViewClickListener { _, _ ->
                true
            }
            .withCompactStyle(true)
            .withProfileImagesVisible(false)
            .withSelectionListEnabledForSingleProfile(false)
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

        viewModel.onViewEvents(lifecycleScope, viewEvents().onStart {
            if (savedInstanceState == null) {
                emit(InitViewEvent)
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        drawer.saveInstanceState(outState)
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
        val countString = when (state.priceAlertsCount) {
            is PriceAlertCount.NotSet -> ""
            is PriceAlertCount.Set -> state.priceAlertsCount.count.toString()
        }

        state.activeRegion?.let {
            findDrawerItem<PrimaryDrawerItem>(R.id.home_drawer_region_selection)
                .withDescription(it.country.displayName())
        }

        findDrawerItem<PrimaryDrawerItem>(R.id.manageWatchlistFragment).withBadge(countString)
        findDrawerItem<SwitchDrawerItem>(R.id.home_drawer_night_mode_switch)
            .withChecked(state.nightModeEnabled)
        drawer.adapter.notifyAdapterDataSetChanged()
    }

    override fun viewEvents(): Flow<HomeMviViewEvent> = viewEventsChannel.consumeAsFlow()

    private fun insertMenuItems() {
        val dealsDrawerItem = PrimaryDrawerItem().withName(R.string.title_on_going_deals)
            .withIdentifier(R.id.dealsFragment.toLong())
            .withIcon(R.drawable.ic_deal)
            .withIconTintingEnabled(true)

        val managedWatchlistDrawerItem = PrimaryDrawerItem().withName(R.string.title_manage_your_watchlist)
            .withIdentifier(R.id.manageWatchlistFragment.toLong())
            .withIcon(R.drawable.ic_add_to_watch_list)
            .withIconTintingEnabled(true)

        val nightModeDrawerItem = SwitchDrawerItem().withName(R.string.enable_night_mode)
            .withIcon(R.drawable.ic_weather_night)
            .withSelectable(false)
            .withIconTintingEnabled(true)
            .withIdentifier(R.id.home_drawer_night_mode_switch.toLong())
            .withOnCheckedChangeListener { _, _, _ ->
                viewEventsChannel.offer(NightModeToggleViewEvent)
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
            .withOnDrawerItemClickListener { _, _, drawerItem ->
                when (drawerItem.identifier.toInt()) {
                    R.id.manageWatchlistFragment, R.id.dealsFragment -> {
                        navigateToDestination(drawerItem.identifier.toInt())
                    }
                }
                false
            }
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

    private fun navigateToDestination(@IdRes identifier: Int) {
        if (identifier == navController.currentDestination?.id) return
        val navOptionsBuilder = navOptions {
            anim {
                popEnter = R.anim.nav_default_pop_enter_anim
                popExit = R.anim.nav_default_pop_exit_anim
                enter = R.anim.nav_default_enter_anim
                exit = R.anim.nav_default_exit_anim
            }
            if (identifier == R.id.dealsFragment) {
                popUpTo(R.id.dealsFragment) { inclusive = false }
            }
        }
        navController.navigate(identifier, null, navOptionsBuilder)
    }

    private inline fun <reified T : IDrawerItem<*, *>> findDrawerItem(@IdRes identifier: Int) =
        drawer.getDrawerItem(identifier.toLong()) as T

    private fun onInject() {
        coreComponent().also {
            DaggerHomeComponent.factory()
                .create(this, it, it.authComponent)
                .inject(this)
        }
    }
}
