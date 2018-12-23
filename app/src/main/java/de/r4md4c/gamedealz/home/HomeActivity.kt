package de.r4md4c.gamedealz.home

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.deals.DealsFragment
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.model.displayName
import de.r4md4c.gamedealz.items.ProgressDrawerItem
import de.r4md4c.gamedealz.regions.RegionSelectionDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class HomeActivity : AppCompatActivity(), LifecycleOwner, DealsFragment.OnFragmentInteractionListener {

    private lateinit var drawer: Drawer

    private val viewModel: HomeViewModel by viewModel()

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
        setSupportActionBar(toolbar)
        loadDrawer(savedInstanceState)

        listenToViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        drawer.saveInstanceState(outState)
    }

    override fun onFragmentInteraction(uri: Uri) {
        Timber.d("$uri")
    }

    private fun listenToViewModel() {
        viewModel.currentRegion.observe(this, Observer {
            accountHeader.setSelectionFirstLine(it.regionCode)
            accountHeader.setSelectionSecondLine(it.country.displayName())
        })

        viewModel.regionsLoading.observe(this, Observer {
            showProgress(it)
        })

        viewModel.openRegionSelectionDialog.observe(this, Observer {
            RegionSelectionDialogFragment.create(it).show(supportFragmentManager, null)
        })

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
        viewModel.init()
    }

    private fun loadDrawer(savedInstanceState: Bundle?) {
        drawer = DrawerBuilder(this)
            .withToolbar(toolbar)
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
}
