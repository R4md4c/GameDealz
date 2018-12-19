package de.r4md4c.gamedealz.deals.datasource

import androidx.lifecycle.LiveData

interface DealsDataSource {

    val loading: LiveData<Boolean>

    fun invalidate()
}