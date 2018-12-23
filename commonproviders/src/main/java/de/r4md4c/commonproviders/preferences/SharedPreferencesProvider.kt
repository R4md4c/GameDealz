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

}