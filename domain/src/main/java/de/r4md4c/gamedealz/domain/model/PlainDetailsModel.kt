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
import kotlinx.android.parcel.Parcelize

/**
 * A data class that aggregates the information retrieved from IsThereAnyDeal and Steam.
 *
 *  @param currencyModel the prices currency that this model has returned.
 *  @param plainId the plain id on IsThereAnyDeal
 *  @param shopPrices a Map between shop as keys, and a pair of its current price and it's Historical low price
 *  @param screenshots a list of image urls from the steam page.
 *  @param headerImage header image that is retrieved from the steam page.
 *  @param aboutGame Under the "About This Game" section on steam page.
 *  @param shortDescription the description that lies under the header image on steam.
 *  @param drmNotice The drm notice that is retrieved from steam.
 */
@Parcelize
data class PlainDetailsModel(
    val currencyModel: CurrencyModel,
    val plainId: String,
    val shopPrices: Map<ShopModel, PriceModelHistoricalLowModelPair>,
    val screenshots: List<ScreenshotModel> = emptyList(),
    val headerImage: String? = null,
    val aboutGame: String? = null,
    val shortDescription: String? = null,
    val drmNotice: String? = null
) : Parcelable

@Parcelize
data class PriceModelHistoricalLowModelPair(val priceModel: PriceModel, val historicalLowModel: HistoricalLowModel?) :
    Parcelable
