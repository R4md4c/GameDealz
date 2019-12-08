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

package de.r4md4c.gamedealz.feature.watchlist.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.r4md4c.commonproviders.di.viewmodel.ViewModelKey
import de.r4md4c.gamedealz.feature.watchlist.AddToWatchListViewModel
import de.r4md4c.gamedealz.feature.watchlist.ManageWatchlistViewModel
import de.r4md4c.gamedealz.feature.watchlist.shortcut.ShortcutManager
import de.r4md4c.gamedealz.feature.watchlist.shortcut.ShortcutManagerImpl

@Module
abstract class WatchlistFeatureModule {

    @Binds
    @IntoMap
    @ViewModelKey(AddToWatchListViewModel::class)
    abstract fun bindsAddToWatchListViewModel(it: AddToWatchListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManageWatchlistViewModel::class)
    abstract fun bindsManageWatchlistViewModel(it: ManageWatchlistViewModel): ViewModel

    @Binds
    internal abstract fun bindsShortcutManager(it: ShortcutManagerImpl): ShortcutManager
}
