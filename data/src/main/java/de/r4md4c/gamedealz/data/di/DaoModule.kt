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

import dagger.Module
import dagger.Provides
import de.r4md4c.gamedealz.data.GameDealzDatabase
import de.r4md4c.gamedealz.data.dao.CountriesDao
import de.r4md4c.gamedealz.data.dao.PlainsDao
import de.r4md4c.gamedealz.data.dao.PriceAlertDao
import de.r4md4c.gamedealz.data.dao.RegionWithCountriesDao
import de.r4md4c.gamedealz.data.dao.StoresDao
import de.r4md4c.gamedealz.data.dao.WatcheeStoreJoinDao
import de.r4md4c.gamedealz.data.dao.WatchlistDao

@Module
object DaoModule {

    @Provides
    internal fun bindsRegionWithCountriesDao(gameDealzDatabase: GameDealzDatabase): RegionWithCountriesDao =
        gameDealzDatabase.regionWithCountriesDao()

    @Provides
    internal fun bindsStoresDao(gameDealzDatabase: GameDealzDatabase): StoresDao =
        gameDealzDatabase.storesDao()

    @Provides
    internal fun bindsPlainDao(gameDealzDatabase: GameDealzDatabase): PlainsDao =
        gameDealzDatabase.plainsDao()

    @Provides
    internal fun bindsCountriesDao(gameDealzDatabase: GameDealzDatabase): CountriesDao =
        gameDealzDatabase.countriesDao()

    @Provides
    internal fun bindsWatchlistDao(gameDealzDatabase: GameDealzDatabase): WatchlistDao =
        gameDealzDatabase.watchlistDao()

    @Provides
    internal fun bindsGameDealzDatabase(gameDealzDatabase: GameDealzDatabase): WatcheeStoreJoinDao =
        gameDealzDatabase.watcheeStoreJoinDao()

    @Provides
    internal fun bindsPriceAlertDao(gameDealzDatabase: GameDealzDatabase): PriceAlertDao =
        gameDealzDatabase.priceAlertDao()
}
