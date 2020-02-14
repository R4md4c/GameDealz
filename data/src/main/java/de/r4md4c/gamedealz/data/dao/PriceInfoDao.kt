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

package de.r4md4c.gamedealz.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.r4md4c.gamedealz.data.entity.HistoricalLowPrice
import de.r4md4c.gamedealz.data.entity.Price
import de.r4md4c.gamedealz.data.model.PriceInfo

@Dao
interface PriceInfoDao {

    @Query(
        """
        SELECT Price.fk_plainId as plainId, 
        Price.newPrice, 
        Price.oldPrice,
        Price.priceCutPercentage,
        Price.dateUpdated,
        Store.name,
        Store.id as storeId,
        HistoricalLowPrice.price as lowestPrice,
        HistoricalLowPrice.priceDate,
        HistoricalLowPrice.priceCutPercentage as historicalLowCutPercentage,
        HistoricalLowPrice.dateUpdated as historicalLowLastUpdate
        FROM Price 
        LEFT JOIN Store ON Price.fk_storeId = Store.id
        LEFT JOIN HistoricalLowPrice ON HistoricalLowPrice.fk_storeId = Store.id
        WHERE Price.fk_plainId = :plainId
        """
    )
    suspend fun currentPrices(plainId: String): List<PriceInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<Price>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoricalLowPrice(historicalLows: List<HistoricalLowPrice>)

    @Transaction
    suspend fun insertIntoPricesAndHistoricalLow(prices: List<Price>,
                                                 historicalLows: List<HistoricalLowPrice>) {
        insertPrices(prices)
        insertHistoricalLowPrice(historicalLows)
    }
}
