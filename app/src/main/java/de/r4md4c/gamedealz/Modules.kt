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
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.paging.DataSource
import de.r4md4c.commonproviders.notification.Notifier
import de.r4md4c.gamedealz.common.navigation.AndroidNavigator
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.common.notifications.ToastViewNotifier
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import de.r4md4c.gamedealz.common.notifications.WatcheesPushNotifier
import de.r4md4c.gamedealz.common.shortcut.ShortcutManager
import de.r4md4c.gamedealz.common.shortcut.ShortcutManagerImpl
import de.r4md4c.gamedealz.common.state.OnRetryClick
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.state.StateVisibilityHandler
import de.r4md4c.gamedealz.common.state.UIStateMachineDelegate
import de.r4md4c.gamedealz.deals.DealsViewModel
import de.r4md4c.gamedealz.deals.datasource.DealsDataSourceFactory
import de.r4md4c.gamedealz.deals.filter.DealsFilterViewModel
import de.r4md4c.gamedealz.deals.model.DealRenderModel
import de.r4md4c.gamedealz.detail.DetailsViewModel
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import de.r4md4c.gamedealz.home.HomeViewModel
import de.r4md4c.gamedealz.regions.RegionSelectionViewModel
import de.r4md4c.gamedealz.search.SearchViewModel
import de.r4md4c.gamedealz.watchlist.AddToWatchListViewModel
import de.r4md4c.gamedealz.watchlist.ManageWatchlistViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module.module

val MAIN = module {

    factory<ViewNotifier> {
        ToastViewNotifier(androidContext())
    }

    factory<ShortcutManager> { ShortcutManagerImpl(androidContext(), get()) }

    factory<DataSource.Factory<Int, DealRenderModel>> { (stateMachineDelegate: StateMachineDelegate) ->
        DealsDataSourceFactory(get(), stateMachineDelegate, get(), get())
    }

    factory<StateMachineDelegate> {
        UIStateMachineDelegate()
    }

    factory<Navigator> { (activity: Activity) ->
        AndroidNavigator(
            activity,
            activity.findNavController(R.id.nav_host_fragment)
        )
    }

    factory<Notifier<WatcheeNotificationModel>> { WatcheesPushNotifier(androidContext(), get()) }

    viewModel {
        val stateMachineDelegate = get<StateMachineDelegate>()
        DealsViewModel(get(), get(parameters = { parametersOf(stateMachineDelegate) }), get(), stateMachineDelegate)
    }

    viewModel<DealsFilterViewModel>()

    viewModel<HomeViewModel>()

    viewModel<SearchViewModel>()

    viewModel<RegionSelectionViewModel>()

    viewModel<AddToWatchListViewModel>()

    viewModel<ManageWatchlistViewModel>()

    viewModel { (activity: Activity) ->
        DetailsViewModel(
            get(),
            get(parameters = { parametersOf(activity) }),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    factory { (fragment: Fragment, onRetry: OnRetryClick) -> StateVisibilityHandler(fragment, onRetry) }

}