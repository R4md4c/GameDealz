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
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An entity that describes a watched game.
 *
 * @param id The local id of the watchee.
 * @param plainId ITAD plain id.
 * @param title the title of the game.
 * @param dateAdded When it was added in the database.
 * @param lastCheckDate the last timestamp that this game was checked in.
 * @param lastFetchedPrice the last fetched price from ITAD.
 * @param lastFetchedStoreName the name of the store that the last fetch got from.
 * @param targetPrice the target price that will trigger the alert.
 * @param regionCode the regionCode that was active when that watchee was added.
 * @param countryCode the countryCode that was active when that watchee was added.
 * @param currencyCode the currency code that was active when that watchee was added.
 */
@Entity(tableName = "Watchlist", indices = [Index(value = ["plainId"], unique = true)])
data class Watchee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plainId: String,
    val title: String,
    val dateAdded: Long,
    val lastCheckDate: Long = 0,
    val lastFetchedPrice: Float,
    val lastFetchedStoreName: String,
    val targetPrice: Float,
    val regionCode: String,
    val countryCode: String,
    val currencyCode: String
)
