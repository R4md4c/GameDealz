package de.r4md4c.commonproviders.configuration

import android.content.Context
import androidx.core.os.ConfigurationCompat
import java.util.*

internal class AndroidConfigurationImpl(private val context: Context) : AndroidConfiguration {

    override val locale: Locale
        get() = ConfigurationCompat.getLocales(context.resources.configuration)[0]
}