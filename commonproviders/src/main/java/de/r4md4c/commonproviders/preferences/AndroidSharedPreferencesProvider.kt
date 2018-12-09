package de.r4md4c.commonproviders.preferences

import android.content.Context
import android.content.SharedPreferences

internal class AndroidSharedPreferencesProvider(context: Context) : SharedPreferencesProvider {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("game_dealz_prefs", Context.MODE_PRIVATE)

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


    private companion object {
        private const val KEY_ACTIVE_REGION_COUNTRY = "active_region_country"
    }
}