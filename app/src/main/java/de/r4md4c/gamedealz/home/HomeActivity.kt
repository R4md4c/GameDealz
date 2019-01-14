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

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.navigation.ui.NavigationUI
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.deals.DealsFragment
import de.r4md4c.gamedealz.domain.model.displayName
import de.r4md4c.gamedealz.home.item.ErrorDrawerItem
import de.r4md4c.gamedealz.regions.RegionSelectionDialogFragment
import de.r4md4c.gamedealz.search.SearchFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class HomeActivity : AppCompatActivity(), DealsFragment.OnFragmentInteractionListener,
    SearchFragment.OnFragmentInteractionListener, RegionSelectionDialogFragment.OnRegionChangeSubmitted {

    private lateinit var drawer: Drawer

    val drawerLayout: DrawerLayout
        get() = drawer.drawerLayout

    private val viewModel: HomeViewModel by viewModel { parametersOf(this) }

    private val navigator: Navigator by inject { parametersOf(this) }

    private val navController
        get() = findNavController(R.id.nav_host_fragment)

    private val accountHeader by lazy {
        AccountHeaderBuilder()
            .withActivity(this)
            .withCompactStyle(true)
            .withOnAccountHeaderSelectionViewClickListener { _, _ -> handleAccountHeaderClick(); true }
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadDrawer(savedInstanceState)
        insertMenuItems()
        listenForDestinationChanges()

        listenToViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        drawer.saveInstanceState(outState)
    }

    override fun onFragmentInteraction(uri: Uri, extras: Parcelable?) {
        viewModel.onNavigateTo(navigator, uri.toString(), extras)
    }

    override fun onSupportNavigateUp(): Boolean =
        NavigationUI.navigateUp(findNavController(R.id.nav_host_fragment), drawer.drawerLayout)

    override fun onRegionSubmitted() {
        viewModel.closeDrawer()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
            return
        }
        super.onBackPressed()
    }

    private fun listenToViewModel() {
        observeCurrentRegion()

        observeRegionsLoading()

        observeRegionSelectionDialog()

        observeCloseDrawer()

        observeErrors()

        viewModel.init()
    }

    private fun observeCloseDrawer() {
        viewModel.closeDrawer.observe(this, Observer {
            drawer.closeDrawer()
        })
    }

    private fun insertMenuItems() {
        val dealsDrawerItem = PrimaryDrawerItem().withName(R.string.title_on_going_deals)
            .withIdentifier(R.id.dealsFragment.toLong())
            .withIcon(R.drawable.ic_deal)
            .withIconTintingEnabled(true)

        val managedWatchlistDrawerItem = PrimaryDrawerItem().withName(R.string.title_manage_your_watchlist)
            .withIdentifier(R.id.manageWatchlistFragment.toLong())
            .withIcon(R.drawable.ic_add_to_watch_list)
            .withIconTintingEnabled(true)

        drawer.setItems(listOf(dealsDrawerItem, managedWatchlistDrawerItem))

        observePriceAlertsUnreadCount()
    }

    private fun observeRegionSelectionDialog() {
        viewModel.openRegionSelectionDialog.observe(this, Observer {
            RegionSelectionDialogFragment.create(it).show(supportFragmentManager, null)
        })
    }

    private fun observeRegionsLoading() {
        viewModel.regionsLoading.observe(this, Observer {
            showProgress(it)
        })
    }

    private fun observePriceAlertsUnreadCount() {
        viewModel.priceAlertsCount.observe(this, Observer { count ->
            (drawer.getDrawerItem(R.id.manageWatchlistFragment.toLong()) as? PrimaryDrawerItem)?.let {
                val adapter = drawer.adapter
                it.withBadge(count)
                adapter.notifyAdapterItemChanged(adapter.getPosition(it.identifier))
            }
        })
    }

    private fun loadDrawer(savedInstanceState: Bundle?) {
        drawer = DrawerBuilder(this)
            .withAccountHeader(accountHeader)
            .withCloseOnClick(true)
            .withHasStableIds(true)
            .withOnDrawerItemClickListener { _, _, drawerItem ->
                navigateToDestination(drawerItem.identifier.toInt())
                false
            }
            .apply { savedInstanceState?.let { withSavedInstance(it) } }
            .build()
    }

    private fun showProgress(show: Boolean) {
//        drawer.removeAllItems()
//        if (show) {
//            drawer.addItem(ProgressDrawerItem())
//        }
    }

    private fun handleAccountHeaderClick() {
        viewModel.onRegionChangeClicked()
    }

    private fun observeCurrentRegion() {
        viewModel.currentRegion.observe(this, Observer {
            accountHeader.setSelectionFirstLine(it.regionCode)
            accountHeader.setSelectionSecondLine(it.country.displayName())
        })
    }

    private fun observeErrors() {
        viewModel.onError.observe(this, Observer {
            drawer.removeAllItems()
            drawer.addItem(ErrorDrawerItem(it) {
                viewModel.init()
            })
        })
    }

    private fun listenForDestinationChanges() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            drawer.setSelection(destination.id.toLong(), false)
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
}
