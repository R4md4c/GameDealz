package de.r4md4c.commonproviders.date

/**
 * A wrapper around the needed functionality of [android.text.format.DateUtils]
 */
interface DateFormatter {

    /**
     * Formats the date according to the provided flags.
     *
     * @param millis Unix timestamp in Millis
     * @param flags Check the FORMAT_* in [android.text.format.DateUtils] for the available values.
     */
    fun formatDateTime(millis: Long, flags: Int): String
}