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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

internal class AndroidSharedPreferencesProvider @Inject constructor(
    context: Context
) : SharedPreferencesProvider {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("game_dealz_prefs", Context.MODE_PRIVATE)

    override val activeRegionAndCountryChannel: Flow<Pair<String, String>>
        get() = callbackFlow {
            val callback = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == KEY_ACTIVE_REGION_COUNTRY) {
                    activeRegionAndCountry?.let {
                        trySend(it)
                    } ?: close()
                }
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(callback)
            awaitClose {
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

    override val reactivePriceCheckerPeriodicIntervalInHours: Flow<Int>
        get() = sharedPreferences.createFlowFromKey(KEY_PERIODIC_PRICE_CHECKER_HOURLY_INTERVAL) { priceCheckerPeriodicIntervalInHours }

    override var priceCheckerPeriodicIntervalInHours: Int
        get() = sharedPreferences.getInt(KEY_PERIODIC_PRICE_CHECKER_HOURLY_INTERVAL, 6)
        set(value) {
            sharedPreferences.edit().putInt(KEY_PERIODIC_PRICE_CHECKER_HOURLY_INTERVAL, value)
                .apply()
        }

    override var activeNightMode: NightMode
        get() = NightMode.fromString(
            sharedPreferences.getString(
                KEY_ACTIVE_NIGHT_MODE,
                NightMode.Disabled.name
            )!!
        )
        set(value) {
            sharedPreferences.edit().putString(KEY_ACTIVE_NIGHT_MODE, value.name).apply()
        }

    override val reactiveNightMode: Flow<NightMode>
        get() = sharedPreferences.createFlowFromKey(KEY_ACTIVE_NIGHT_MODE) { activeNightMode }

    override var userName: String
        get() = sharedPreferences.getString(KEY_USER, "")!!
        set(value) {
            sharedPreferences.edit().putString(KEY_USER, value).apply()
        }

    override fun clearUser() {
        sharedPreferences.edit().remove(KEY_USER).apply()
    }

    override val userAsFlow: Flow<String>
        get() = sharedPreferences.createFlowFromKey(KEY_USER) { userName }

    private fun <T> SharedPreferences.createFlowFromKey(key: String, value: () -> T): Flow<T> =
        callbackFlow {
            trySend(value())

            val callback = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                if (changedKey == key) {
                    trySend(value())
                }
            }
            registerOnSharedPreferenceChangeListener(callback)
            awaitClose {
                unregisterOnSharedPreferenceChangeListener(callback)
            }
        }

    private companion object {
        private const val KEY_ACTIVE_NIGHT_MODE = "current_active_night_mode"
        private const val KEY_PERIODIC_PRICE_CHECKER_HOURLY_INTERVAL =
            "price_checker_hourly_interval"
        private const val KEY_ACTIVE_REGION_COUNTRY = "active_region_country"
        private const val KEY_USER = "user_data"
    }
}
