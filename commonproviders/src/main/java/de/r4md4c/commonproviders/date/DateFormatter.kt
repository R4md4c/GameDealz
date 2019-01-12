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

package de.r4md4c.commonproviders.date

import android.text.format.DateUtils.MINUTE_IN_MILLIS

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

    /**
     * Get relative time to the provided millis, eg. 5 minutes ago.
     * @param minResolution the minimum timespan to report. For example MINUTES_IN_MILLIS yields "minutes ago".
     */
    fun getRelativeTimeSpanString(millis: Long, minResolution: Long = MINUTE_IN_MILLIS): String
}