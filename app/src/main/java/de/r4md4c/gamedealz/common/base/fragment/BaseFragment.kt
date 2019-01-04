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

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.home.HomeActivity
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_deals.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.android.inject

abstract class BaseFragment : Fragment() {
    protected val dispatchers: IDispatchers by inject()

    private val job = SupervisorJob()
    private val viewScopeDelegate = lazy { CoroutineScope(job + dispatchers.Main) }

    /**
     * A [CoroutineScope] that binds to the view, that is, clears on [onDestroyView].
     * You should use that if you want to be sure that the views that you're touching are valid.
     */
    protected val viewScope by viewScopeDelegate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clearFindViewByIdCache()
        toolbar?.let { onCreateOptionsMenu(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (viewScopeDelegate.isInitialized()) {
            job.cancel()
        }
    }

    val drawerLayout: DrawerLayout?
        get() = (activity as? HomeActivity)?.drawerLayout

    open fun onCreateOptionsMenu(toolbar: Toolbar) = Unit
}
