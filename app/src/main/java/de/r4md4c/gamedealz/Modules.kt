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

package de.r4md4c.gamedealz

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import de.r4md4c.gamedealz.common.navigation.AndroidNavigator
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.common.notifications.ToastViewNotifier
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.state.UIStateMachineDelegate
import de.r4md4c.gamedealz.home.HomeViewModel
import de.r4md4c.gamedealz.regions.RegionSelectionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.dsl.module.module

val MAIN = module {

    factory<ViewNotifier> {
        ToastViewNotifier(androidContext())
    }

    factory<StateMachineDelegate> {
        UIStateMachineDelegate()
    }

    factory<Navigator> { (activity: Activity) ->
        AndroidNavigator(
            activity as FragmentActivity,
            dagger.Lazy { activity.findNavController(R.id.nav_host_fragment) }
        )
    }

    viewModel<HomeViewModel>()

    viewModel<RegionSelectionViewModel>()

}
