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

import android.content.Context
import android.content.SharedPreferences
import de.r4md4c.commonproviders.appcompat.NightMode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel

internal class AndroidSharedPreferencesProvider(context: Context) : SharedPreferencesProvider {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("game_dealz_prefs", Context.MODE_PRIVATE)

    override val activeRegionAndCountryChannel: ReceiveChannel<Pair<String, String>>
        get() = Channel<Pair<String, String>>(Channel.CONFLATED).also { channel ->
            val callback = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == KEY_ACTIVE_REGION_COUNTRY) {
                    activeRegionAndCountry?.let {
                        channel.offer(it)
                    } ?: channel.close()
                }
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(callback)
            channel.invokeOnClose {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(callback)
            }
        }


    override var activeRegionAndCountry: Pair<String, String>?
        get() = sharedPreferences.getString(KEY_ACTIVE_REGION_COUNTRY, null)
            ?.split('|')
            ?.run {
                this[0] to this[1]
            }
        set(value) {
            value?.let {
                sharedPreferences.edit()
                    .putString(KEY_ACTIVE_REGION_COUNTRY, "${it.first}|${it.second}")
                    .apply()
            }
        }

    override val reactivePriceCheckerPeriodicIntervalInHours: ReceiveChannel<Int>
        get() = sharedPreferences.createReceiveChannelFromKey(KEY_PERIODIC_PRICE_CHECKER_HOURLY_INTERVAL)
        { priceCheckerPeriodicIntervalInHours }

    override var priceCheckerPeriodicIntervalInHours: Int
        get() = sharedPreferences.getInt(KEY_PERIODIC_PRICE_CHECKER_HOURLY_INTERVAL, 6)
        set(value) {
            sharedPreferences.edit().putInt(KEY_PERIODIC_PRICE_CHECKER_HOURLY_INTERVAL, value).apply()
        }

    override var activeNightMode: NightMode
        get() = NightMode.fromString(sharedPreferences.getString(KEY_ACTIVE_NIGHT_MODE, NightMode.Disabled.name)!!)
        set(value) {
            sharedPreferences.edit().putString(KEY_ACTIVE_NIGHT_MODE, value.name).apply()
        }

    override val reactiveNightMode: ReceiveChannel<NightMode>
        get() = sharedPreferences.createReceiveChannelFromKey(KEY_ACTIVE_NIGHT_MODE) { activeNightMode }

    private fun <T> SharedPreferences.createReceiveChannelFromKey(key: String, value: () -> T): ReceiveChannel<T> =
        ConflatedBroadcastChannel(value())
            .also { channel ->
                val callback = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                    if (changedKey == key) {
                        channel.offer(value())
                    }
                }
                registerOnSharedPreferenceChangeListener(callback)
                channel.invokeOnClose {
                    unregisterOnSharedPreferenceChangeListener(callback)
                }
            }.openSubscription()

    private companion object {
        private const val KEY_ACTIVE_NIGHT_MODE = "current_active_night_mode"
        private const val KEY_PERIODIC_PRICE_CHECKER_HOURLY_INTERVAL = "price_checker_hourly_interval"
        private const val KEY_ACTIVE_REGION_COUNTRY = "active_region_country"
    }
}