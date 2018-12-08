package de.r4md4c.gamedealz.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import de.r4md4c.gamedealz.R
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var drawer: Drawer

    private val viewModel: HomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
    }

    private fun loadDrawer() {
        drawer = DrawerBuilder(this)
            .withToolbar(toolbar)
            .build()
    }
}
