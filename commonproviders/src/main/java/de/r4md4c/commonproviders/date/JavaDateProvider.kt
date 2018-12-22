package de.r4md4c.commonproviders.date

import java.util.*

class JavaDateProvider() : DateProvider {

    override fun now(): Date = Date()

    override fun today(): Date {
        val calendar = Calendar.getInstance()
        with(calendar) {
            time = now()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            return time
        }
    }

}