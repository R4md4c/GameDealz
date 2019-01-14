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

package de.r4md4c.gamedealz.domain.model

import android.os.Parcelable
import de.r4md4c.gamedealz.data.entity.Watchee
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WatcheeModel(
    val id: Long? = null,
    val plainId: String,
    val title: String,
    val dateAdded: Long = 0,
    val lastCheckDate: Long = 0,
    val currentPrice: Float,
    val targetPrice: Float,
    val regionCode: String,
    val countryCode: String,
    val currencyCode: String
) : Parcelable

internal fun Watchee.toModel() =
    WatcheeModel(
        id,
        plainId,
        title,
        dateAdded,
        lastCheckDate,
        currentPrice,
        targetPrice,
        regionCode,
        countryCode,
        currencyCode
    )

internal fun WatcheeModel.toRepositoryModel() =
    Watchee(
        id ?: 0,
        plainId,
        title,
        dateAdded,
        lastCheckDate,
        currentPrice,
        targetPrice,
        regionCode,
        countryCode,
        currencyCode
    )
