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

package de.r4md4c.gamedealz.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * A Minimal representation of the Price and HistoricalLow queries.
 */
data class PriceInfo(
    val plainId: String,
    val newPrice: Float,
    val oldPrice: Float,
    val priceCutPercentage: Short,
    val dateUpdated: Long,
    @Embedded val storeInfo: StoreInfo,
    @Embedded val historicalLowInfo: HistoricalLowInfo?
) {
    data class StoreInfo(@ColumnInfo(name = "storeId") val id: String, val name: String)

    data class HistoricalLowInfo(val lowestPrice: Float,
                                 @ColumnInfo(name = "historicalLowCutPercentage") val priceCutPercentage: Short,
                                 val wasAt: Long,
                                 @ColumnInfo(name = "historicalLowLastUpdate") val dateUpdated: Long)
}
