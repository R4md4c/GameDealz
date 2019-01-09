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

package de.r4md4c.commonproviders.preferences

import kotlinx.coroutines.channels.ReceiveChannel

/**
 * A wrapper around [android.content.SharedPreferences].
 */
interface SharedPreferencesProvider {

    /**
     * Returns a channel that emits always when active region is changed.
     *
     * @return A pair that has region as first and country as second, otherwise null.
     */
    val activeRegionAndCountryChannel: ReceiveChannel<Pair<String, String>>

    /**
     * Returns the current active region and country.
     *
     * @return A pair that has region as first and country as second, otherwise null.
     */
    var activeRegionAndCountry: Pair<String, String>?

    /**
     * Returns the periodic hourly interval in which the price checker runs. By default the checker runs every 6 hours
     */
    val reactivePriceCheckerPeriodicIntervalInHours: ReceiveChannel<Int>

    var priceCheckerPeriodicIntervalInHours: Int
}