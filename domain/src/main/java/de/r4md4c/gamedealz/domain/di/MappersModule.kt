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

package de.r4md4c.gamedealz.domain.di

import dagger.Binds
import dagger.Module
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.domain.mapper.AppDetailsDTOGameArtworkMapper
import de.r4md4c.gamedealz.domain.mapper.HistoricalLowDTOMapper
import de.r4md4c.gamedealz.domain.mapper.Mapper
import de.r4md4c.gamedealz.domain.mapper.PriceDTOMapperToPriceModelMapper
import de.r4md4c.gamedealz.domain.mapper.StoreEntityToShopModelMapper
import de.r4md4c.gamedealz.domain.model.HistoricalLowModel
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.ShopModel
import de.r4md4c.gamedealz.network.model.HistoricalLowDTO
import de.r4md4c.gamedealz.network.model.PriceDTO
import de.r4md4c.gamedealz.network.model.steam.AppDetailsDTO

@Module
internal abstract class MappersModule {

    @Binds
    abstract fun bindsHistoricalLowDTOMapper(it: HistoricalLowDTOMapper)
            : Mapper<HistoricalLowDTO, HistoricalLowModel>

    @Binds
    abstract fun bindsPriceDTOMapper(it: PriceDTOMapperToPriceModelMapper)
            : Mapper<PriceDTO, PriceModel>

    @Binds
    abstract fun bindsStoreEntityMapper(it: StoreEntityToShopModelMapper)
            : Mapper<Store, ShopModel>

    @Binds
    abstract fun bindsAppDetailsDTOGameArtworkMapper(it: AppDetailsDTOGameArtworkMapper)
            : Mapper<AppDetailsDTO, PlainDetailsModel.GameArtworkDetails>
}
