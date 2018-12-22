package de.r4md4c.commonproviders.date

import java.util.*

interface DateProvider {

    /**
     * Provide now's date with time.
     */
    fun now(): Date

    /**
     * Provide today's date without any time information.
     */
    fun today(): Date
}
