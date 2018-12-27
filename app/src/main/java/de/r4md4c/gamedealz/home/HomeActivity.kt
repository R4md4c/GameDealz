package de.r4md4c.gamedealz.home

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.navigator.Navigator
import de.r4md4c.gamedealz.common.viewholder.ProgressDrawerItem
import de.r4md4c.gamedealz.deals.DealsFragment
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.model.displayName
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

        listenToViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        drawer.saveInstanceState(outState)
    }

    override fun onFragmentInteraction(uri: Uri) {
        viewModel.onNavigateTo(navigator, uri.toString())
    }

    override fun onSupportNavigateUp(): Boolean =
        NavigationUI.navigateUp(findNavController(R.id.nav_host_fragment), drawer.drawerLayout)

    override fun onRegionSubmitted() {
        viewModel.closeDrawer()
    }

    private fun listenToViewModel() {
        observeCurrentRegion()

        observeRegionsLoading()

        observeRegioNSelectionDialog()

        observeStoreSelections()

        observeCloseDrawer()

        viewModel.init()
    }

    private fun observeCloseDrawer() {
        viewModel.closeDrawer.observe(this, Observer {
            drawer.closeDrawer()
        })
    }

    private fun observeStoreSelections() {
        viewModel.stores.observe(this, Observer { storeList ->
            val drawerItems = storeList.map {
                PrimaryDrawerItem()
                    .withName(it.name)
                    .withTag(it)
                    .withIdentifier(it.id.hashCode().toLong())
                    .withSetSelected(it.selected)
                    .withOnDrawerItemClickListener { _, _, drawerItem ->
                        viewModel.onStoreSelected(drawerItem.tag as StoreModel)
                        true
                    }
            }
            drawer.setItems(drawerItems)
        })
    }

    private fun observeRegioNSelectionDialog() {
        viewModel.openRegionSelectionDialog.observe(this, Observer {
            RegionSelectionDialogFragment.create(it).show(supportFragmentManager, null)
        })
    }

    private fun observeRegionsLoading() {
        viewModel.regionsLoading.observe(this, Observer {
            showProgress(it)
        })
    }

    private fun loadDrawer(savedInstanceState: Bundle?) {
        drawer = DrawerBuilder(this)
            .withAccountHeader(accountHeader)
            .withMultiSelect(true)
            .withCloseOnClick(false)
            .withHasStableIds(true)
            .apply { savedInstanceState?.let { withSavedInstance(it) } }
            .build()
    }

    private fun showProgress(show: Boolean) {
        drawer.removeAllItems()
        if (show) {
            drawer.addItem(ProgressDrawerItem())
        }
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
}
