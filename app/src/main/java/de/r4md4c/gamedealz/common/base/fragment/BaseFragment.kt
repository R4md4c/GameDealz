package de.r4md4c.gamedealz.common.base.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import de.r4md4c.gamedealz.SCOPE_FRAGMENT
import de.r4md4c.gamedealz.home.HomeActivity
import kotlinx.android.synthetic.main.fragment_deals.*
import org.koin.androidx.scope.ext.android.bindScope
import org.koin.androidx.scope.ext.android.getOrCreateScope

abstract class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindScope(getOrCreateScope(SCOPE_FRAGMENT))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar?.let { onCreateOptionsMenu(it) }
    }

    val drawerLayout: DrawerLayout?
        get() = (activity as? HomeActivity)?.drawerLayout

    open fun onCreateOptionsMenu(toolbar: Toolbar) = Unit
}
