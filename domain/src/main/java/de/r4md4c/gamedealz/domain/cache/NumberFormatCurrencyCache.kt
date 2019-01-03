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

package de.r4md4c.gamedealz.domain.cache

import java.text.NumberFormat
import java.util.concurrent.ConcurrentHashMap

/**
 * Used to Cache NumberFormat's Currency instances to reuse instances when formatting currencies.
 */
internal object NumberFormatCurrencyCache {
    private val numberFormatCache by lazy { ConcurrentHashMap(mutableMapOf<String, NumberFormat>()) }

    /**
     * Gets the number format corresponding to the currency code.
     *
     * @param currencyCode the currency code to get the NumberFormat for..
     */
    internal operator fun get(currencyCode: String): NumberFormat =
        numberFormatCache.getOrPut(currencyCode) {
            NumberFormat.getCurrencyInstance().apply {
                val currency = java.util.Currency.getInstance(currencyCode)
                this.currency = currency
            }
        }

}
