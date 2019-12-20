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

package de.r4md4c.gamedealz.network.di

import dagger.Binds
import dagger.Module
import de.r4md4c.gamedealz.network.repository.DealsRemoteRepository
import de.r4md4c.gamedealz.network.repository.IsThereAnyDealRepository
import de.r4md4c.gamedealz.network.repository.PlainsRemoteRepository
import de.r4md4c.gamedealz.network.repository.PricesRemoteRepository
import de.r4md4c.gamedealz.network.repository.RegionsRemoteRepository
import de.r4md4c.gamedealz.network.repository.SteamRemoteRepository
import de.r4md4c.gamedealz.network.repository.SteamRepository
import de.r4md4c.gamedealz.network.repository.StoresRemoteRepository
import de.r4md4c.gamedealz.network.repository.UserRemoteRepository
import de.r4md4c.gamedealz.network.service.IsThereAnyDealScrappingService
import de.r4md4c.gamedealz.network.service.SearchService

@Module
abstract class RemoteRepositoryModule {

    @Binds
    internal abstract fun bindsPlainsRemoteRepo(it: IsThereAnyDealRepository): PlainsRemoteRepository

    @Binds
    internal abstract fun bindsPricesRemoteRepository(it: IsThereAnyDealRepository): PricesRemoteRepository

    @Binds
    internal abstract fun bindsRegionsRemoteRepository(it: IsThereAnyDealRepository): RegionsRemoteRepository

    @Binds
    internal abstract fun bindsStoresRemoteRepository(it: IsThereAnyDealRepository): StoresRemoteRepository

    @Binds
    internal abstract fun bindsDealsRemoteRepository(it: IsThereAnyDealRepository): DealsRemoteRepository

    @Binds
    internal abstract fun bindsUserRemoteRepository(it: IsThereAnyDealRepository): UserRemoteRepository

    @Binds
    internal abstract fun bindsSearchService(it: IsThereAnyDealScrappingService): SearchService

    @Binds
    internal abstract fun bindsSteamRemoteRepository(it: SteamRepository): SteamRemoteRepository
}
