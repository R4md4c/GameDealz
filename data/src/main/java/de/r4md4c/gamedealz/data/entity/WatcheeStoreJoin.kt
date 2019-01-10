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

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * The relationship table between [Watchee] and [Store].
 */
@Entity(
    tableName = "watchlist_store_join",
    primaryKeys = ["watcheeId", "storeId"],
    foreignKeys = [ForeignKey(
        entity = Watchee::class,
        parentColumns = ["id"],
        childColumns = ["watcheeId"],
        onDelete = ForeignKey.CASCADE
    ),
        ForeignKey(
            entity = Store::class,
            parentColumns = ["id"],
            childColumns = ["storeId"]
        )],
    indices = [Index(value = ["storeId"])]
)
internal data class WatcheeStoreJoin(
    val watcheeId: Long,
    val storeId: String
)


data class WatcheeWithStores(val watchee: Watchee, val stores: Set<Store>)
