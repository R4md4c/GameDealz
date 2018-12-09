package de.r4md4c.gamedealz.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.displayName
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var drawer: Drawer

    private val viewModel: HomeViewModel by viewModel()

    private val accountHeader by lazy {
        AccountHeaderBuilder()
            .withActivity(this)
            .withCompactStyle(true)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        loadDrawer()
        listenToViewModel()
    }

    private fun listenToViewModel() {
        viewModel.currentRegion.observe(this, Observer {
            accountHeader.setSelectionFirstLine(it.region.regionCode)
            accountHeader.setSelectionSecondLine(it.country.displayName())
        })

        viewModel.retrieveRegions()
    }

    private fun loadDrawer() {
        drawer = DrawerBuilder(this)
            .withToolbar(toolbar)
            .withAccountHeader(accountHeader)
            .build()
    }
}
