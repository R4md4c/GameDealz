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
import kotlinx.parcelize.Parcelize

/**
 * Contains information needed to render notifications.
 *
 * @param watcheeModel the watchee that needs to be notification.
 * @param priceModel the price model that contains the shop information that triggered the alert.
 * @param currencyModel the currency that will be used to display the price.
 */
@Parcelize
data class WatcheeNotificationModel(
    val watcheeModel: WatcheeModel,
    val priceModel: PriceModel,
    val currencyModel: CurrencyModel
) : Parcelable
