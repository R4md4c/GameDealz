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

typealias DealsResult = Pair<Int, List<DealModel>>

data class DealModel(
    val gameId: String,
    val title: String,
    val newPrice: Float,
    val oldPrice: Float,
    val priceCutPercentage: Short,
    val shop: ShopModel,
    val urls: Urls,
    val added: Long,
    val drm: Set<String>,
    val currencyModel: CurrencyModel
)
