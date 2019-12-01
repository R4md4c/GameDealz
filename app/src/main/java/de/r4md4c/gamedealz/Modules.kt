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
import androidx.paging.DataSource
import de.r4md4c.commonproviders.FOR_APPLICATION
import de.r4md4c.commonproviders.notification.Notifier
import de.r4md4c.gamedealz.common.navigation.AndroidNavigator
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.common.notifications.ToastViewNotifier
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import de.r4md4c.gamedealz.common.notifications.WatcheesPushNotifier
import de.r4md4c.gamedealz.common.shortcut.ShortcutManager
import de.r4md4c.gamedealz.common.shortcut.ShortcutManagerImpl
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.state.UIStateMachineDelegate
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import de.r4md4c.gamedealz.feature.deals.datasource.DealsDataSourceFactory
import de.r4md4c.gamedealz.feature.deals.model.DealRenderModel
import de.r4md4c.gamedealz.home.HomeViewModel
import de.r4md4c.gamedealz.regions.RegionSelectionViewModel
import de.r4md4c.gamedealz.watchlist.AddToWatchListViewModel
import de.r4md4c.gamedealz.watchlist.ManageWatchlistViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val MAIN = module {

    factory<ViewNotifier> {
        ToastViewNotifier(androidContext())
    }

    factory<ShortcutManager> { ShortcutManagerImpl(androidContext(), get(name = FOR_APPLICATION)) }

    factory<DataSource.Factory<Int, DealRenderModel>> { (stateMachineDelegate: StateMachineDelegate) ->
        DealsDataSourceFactory(get(), stateMachineDelegate, get(name = FOR_APPLICATION))
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

    factory<Notifier<WatcheeNotificationModel>> { WatcheesPushNotifier(androidContext(), get(name = FOR_APPLICATION)) }

    viewModel<HomeViewModel>()

    viewModel<RegionSelectionViewModel>()

    viewModel {
        AddToWatchListViewModel(get(), get(name = FOR_APPLICATION), get(), get(), get())
    }

    viewModel<ManageWatchlistViewModel>()

}
