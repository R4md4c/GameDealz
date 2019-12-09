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
import de.r4md4c.gamedealz.data.repository.CountriesLocalRepository
import de.r4md4c.gamedealz.data.repository.CountriesRepository
import de.r4md4c.gamedealz.data.repository.PlainsLocalRepository
import de.r4md4c.gamedealz.data.repository.PlainsRepository
import de.r4md4c.gamedealz.data.repository.PriceAlertLocalRepository
import de.r4md4c.gamedealz.data.repository.PriceAlertRepository
import de.r4md4c.gamedealz.data.repository.RegionLocalRepository
import de.r4md4c.gamedealz.data.repository.RegionsRepository
import de.r4md4c.gamedealz.data.repository.StoresLocalRepository
import de.r4md4c.gamedealz.data.repository.StoresRepository
import de.r4md4c.gamedealz.data.repository.WatchlistLocalRepository
import de.r4md4c.gamedealz.data.repository.WatchlistRepository
import de.r4md4c.gamedealz.data.repository.WatchlistStoresRepository

@Module
abstract class RepositoryModule {

    @Binds
    internal abstract fun bindsPriceAlertLocalRepository(it: PriceAlertLocalRepository): PriceAlertRepository

    @Binds
    internal abstract fun bindsWatchlistRepository(it: WatchlistLocalRepository): WatchlistRepository

    @Binds
    internal abstract fun bindsWatchlistStoresRepository(it: WatchlistLocalRepository): WatchlistStoresRepository

    @Binds
    internal abstract fun bindsPlainsRepository(it: PlainsLocalRepository): PlainsRepository

    @Binds
    internal abstract fun bindsRegionLocalRepository(it: RegionLocalRepository): RegionsRepository

    @Binds
    internal abstract fun bindsStoresRepository(it: StoresLocalRepository): StoresRepository

    @Binds
    internal abstract fun bindsCountriesLocalRepository(it: CountriesLocalRepository): CountriesRepository
}
