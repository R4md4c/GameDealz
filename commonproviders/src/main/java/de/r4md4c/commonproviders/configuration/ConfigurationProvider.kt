package de.r4md4c.commonproviders.configuration

import java.util.*

/**
 * A Wrapper around needed functionality from [android.content.res.Configuration].
 */
interface ConfigurationProvider {

    val locale: Locale
}