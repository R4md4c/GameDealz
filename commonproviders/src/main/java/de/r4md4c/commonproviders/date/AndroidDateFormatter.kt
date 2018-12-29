package de.r4md4c.commonproviders.date

import android.content.Context
import android.text.format.DateUtils

class AndroidDateFormatter(private val context: Context) : DateFormatter {

    override fun formatDateTime(millis: Long, flags: Int): String =
        DateUtils.formatDateTime(context, millis, flags)

}