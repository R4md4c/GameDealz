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

package de.r4md4c.gamedealz.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

//TODO: Clear prices and Historical low tables when changing regions.
@Entity(
    indices = [
        Index(value = ["fk_plainId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Store::class,
            parentColumns = ["id"],
            childColumns = ["fk_storeId"]
        ),
        ForeignKey(
            entity = Plain::class,
            parentColumns = ["id"],
            childColumns = ["fk_plainId"]
        )],
    primaryKeys = ["fk_plainId", "fk_storeId"]
)
data class HistoricalLowPrice(
    @ColumnInfo(name = "fk_plainId") val plainId: String,
    @ColumnInfo(name = "fk_storeId") val storeId: String,
    val price: Float,
    val priceCutPercentage: Short,
    val priceDate: Long,
    val dateCreated: Long,
    val dateUpdated: Long
)
