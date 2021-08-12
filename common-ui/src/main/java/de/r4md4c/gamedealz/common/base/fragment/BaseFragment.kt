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

package de.r4md4c.gamedealz.common.base.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.R
import de.r4md4c.gamedealz.common.base.HasDrawerLayout
import de.r4md4c.gamedealz.core.CoreComponent
import de.r4md4c.gamedealz.core.coreComponent
import javax.inject.Inject

abstract class BaseFragment : Fragment() {

    @Inject
    lateinit var dispatchers: IDispatchers

    private val toolbar: Toolbar
        get() = requireView().findViewById(R.id.toolbar) as Toolbar

    override fun onAttach(context: Context) {
        onInject(context.coreComponent())
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onCreateOptionsMenu(toolbar)
    }

    val drawerLayout: DrawerLayout?
        get() = (activity as? HasDrawerLayout)?.drawerLayout

    open fun onCreateOptionsMenu(toolbar: Toolbar) = Unit

    protected open fun onInject(coreComponent: CoreComponent) {}
}
