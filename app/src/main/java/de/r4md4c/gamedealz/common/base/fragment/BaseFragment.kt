package de.r4md4c.gamedealz.common.base.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import de.r4md4c.gamedealz.home.HomeActivity
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_deals.*

abstract class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clearFindViewByIdCache()
        toolbar?.let { onCreateOptionsMenu(it) }
    }

    val drawerLayout: DrawerLayout?
        get() = (activity as? HomeActivity)?.drawerLayout

    open fun onCreateOptionsMenu(toolbar: Toolbar) = Unit
}
