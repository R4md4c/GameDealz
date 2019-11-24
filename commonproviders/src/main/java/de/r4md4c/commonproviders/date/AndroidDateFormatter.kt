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

import android.content.Context
import android.text.format.DateUtils

internal class AndroidDateFormatter(private val context: Context) : DateFormatter {

    override fun formatDateTime(millis: Long, flags: Int): String =
        DateUtils.formatDateTime(context, millis, flags)

    override fun getRelativeTimeSpanString(millis: Long, minResolution: Long): String =
        DateUtils.getRelativeTimeSpanString(millis, System.currentTimeMillis(), minResolution).toString()
}
