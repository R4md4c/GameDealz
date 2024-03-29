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
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    indices = [Index(value = ["fk_currencyCode"])],
    foreignKeys = [ForeignKey(
        entity = Currency::class,
        childColumns = ["fk_currencyCode"],
        parentColumns = ["currencyCode"],
        deferred = true,
        onDelete = CASCADE
    )]
)
data class Region(@PrimaryKey val regionCode: String, @ColumnInfo(name = "fk_currencyCode") val currencyCode: String)

data class RegionWithCountries(
    @Embedded
    val region: Region,

    @Embedded
    val currency: Currency,

    @Relation(entity = Country::class, entityColumn = "regionCode", parentColumn = "regionCode")
    val countries: Set<Country>
)
