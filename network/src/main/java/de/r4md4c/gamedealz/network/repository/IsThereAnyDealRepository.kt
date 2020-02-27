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

package de.r4md4c.gamedealz.network.repository

import de.r4md4c.gamedealz.network.model.AccessToken
import de.r4md4c.gamedealz.network.model.Deal
import de.r4md4c.gamedealz.network.model.HistoricalLowDTO
import de.r4md4c.gamedealz.network.model.PageResult
import de.r4md4c.gamedealz.network.model.PriceDTO
import de.r4md4c.gamedealz.network.model.Store
import de.r4md4c.gamedealz.network.model.User
import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import de.r4md4c.gamedealz.network.service.RegionCodes
import de.r4md4c.gamedealz.network.service.ShopPlains
import javax.inject.Inject

internal class IsThereAnyDealRepository @Inject constructor(
    private val service: IsThereAnyDealService
) : RegionsRemoteRepository, StoresRemoteRepository,
    DealsRemoteRepository, PlainsRemoteRepository,
    PricesRemoteDataSource, UserRemoteRepository {

    override suspend fun regions(): RegionCodes = service.regions().await().data

    override suspend fun stores(region: String, country: String?): List<Store> =
        service.stores(region, country).await().data

    override suspend fun deals(
        offset: Int,
        limit: Int,
        region: String,
        country: String,
        shops: Set<String>
    ): PageResult<Deal> =
        service.deals(
            offset = offset,
            limit = limit,
            region = region,
            country = country,
            shops = shops.toCommaSeparated()
        ).await().run {
            PageResult(this.data.count ?: 0, this.data.list)
        }

    override suspend fun plainsList(shops: Set<String>): ShopPlains =
        service.allPlains(shops = shops.fold("") { acc, value -> "$acc$value," }).await().data

    override suspend fun retrievesPrices(
        plainIds: Set<String>,
        shops: Set<String>,
        regionCode: String?,
        countryCode: String?,
        added: Long?
    ): Map<String, List<PriceDTO>> =
        service.prices(
            plains = plainIds.toCommaSeparated(),
            shops = shops.toCommaSeparated(),
            region = regionCode,
            country = countryCode,
            added = added
        ).await().data.mapValues { it.value.list }

    override suspend fun historicalLow(
        plainIds: Set<String>,
        shops: Set<String>,
        regionCode: String?,
        countryCode: String?
    ): Map<String, HistoricalLowDTO> =
        service.historicalLow(
            plains = plainIds.toCommaSeparated(),
            shops = shops.toCommaSeparated(),
            region = regionCode,
            country = countryCode
        ).await().data

    override suspend fun user(token: AccessToken): User = service.userInfo(token.accessToken).run {
        data["username"]?.let { User.KnownUser(it) } ?: User.UnknownUser
    }

    private fun Set<String>.toCommaSeparated() =
        foldIndexed("") { index, acc, value -> "$acc$value${if (index == size - 1) "" else ","}" }
}
