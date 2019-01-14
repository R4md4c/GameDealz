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
import androidx.room.PrimaryKey

/**
 * A table that will hold information about [Watchee] that have reached their target price and needs user to check them.
 * @param id the primary key of that alert.
 * @param watcheeId the Watchee id that this alert refers to
 * @param buyUrl the buy url that this alert points to.
 * @param storeName the store name that this alert was created from.
 * @param dateCreated the timestamp in seconds where this alert was created.
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Watchee::class,
            parentColumns = ["id"],
            childColumns = ["watcheeId"],
            onDelete = ForeignKey.CASCADE
        )],
    indices = [Index(value = ["watcheeId"], unique = true)]
)
data class PriceAlert(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val watcheeId: Long,
    val buyUrl: String,
    val storeName: String,
    val dateCreated: Long
)
