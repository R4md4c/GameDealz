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

package de.r4md4c.gamedealz.data.di

import dagger.Binds
import dagger.Module
import de.r4md4c.gamedealz.data.repository.CountriesLocalDataSource
import de.r4md4c.gamedealz.data.repository.CountriesLocalDataSourceImpl
import de.r4md4c.gamedealz.data.repository.PlainsLocalDataSource
import de.r4md4c.gamedealz.data.repository.PlainsLocalDataSourceImpl
import de.r4md4c.gamedealz.data.repository.PriceAlertLocalDataSource
import de.r4md4c.gamedealz.data.repository.PriceAlertLocalDataSourceImpl
import de.r4md4c.gamedealz.data.repository.RegionLocalDataSourceImpl
import de.r4md4c.gamedealz.data.repository.RegionsLocalDataSource
import de.r4md4c.gamedealz.data.repository.StoresLocalDataSource
import de.r4md4c.gamedealz.data.repository.StoresLocalDataSourceImpl
import de.r4md4c.gamedealz.data.repository.WatchlistLocalDataSource
import de.r4md4c.gamedealz.data.repository.WatchlistLocalDataSourceImpl
import de.r4md4c.gamedealz.data.repository.WatchlistStoresDataSource

@Module
abstract class RepositoryModule {

    @Binds
    internal abstract fun bindsPriceAlertLocalRepository(it: PriceAlertLocalDataSourceImpl): PriceAlertLocalDataSource

    @Binds
    internal abstract fun bindsWatchlistRepository(it: WatchlistLocalDataSourceImpl): WatchlistLocalDataSource

    @Binds
    internal abstract fun bindsWatchlistStoresRepository(it: WatchlistLocalDataSourceImpl): WatchlistStoresDataSource

    @Binds
    internal abstract fun bindsPlainsRepository(it: PlainsLocalDataSourceImpl): PlainsLocalDataSource

    @Binds
    internal abstract fun bindsRegionLocalRepository(it: RegionLocalDataSourceImpl): RegionsLocalDataSource

    @Binds
    internal abstract fun bindsStoresRepository(it: StoresLocalDataSourceImpl): StoresLocalDataSource

    @Binds
    internal abstract fun bindsCountriesLocalRepository(it: CountriesLocalDataSourceImpl): CountriesLocalDataSource
}
