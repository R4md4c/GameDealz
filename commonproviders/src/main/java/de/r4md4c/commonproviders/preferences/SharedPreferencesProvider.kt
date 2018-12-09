package de.r4md4c.commonproviders.preferences

/**
 * A wrapper around [android.content.SharedPreferences].
 */
interface SharedPreferencesProvider {

    /**
     * Returns the current active region and country.
     *
     * @return A pair that has region as first and country as second, otherwise null.
     */
    var activeRegionAndCountry: Pair<String, String>?

}