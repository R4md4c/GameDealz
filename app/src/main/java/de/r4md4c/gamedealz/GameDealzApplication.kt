package de.r4md4c.gamedealz

import android.app.Application
import de.r4md4c.gamedealz.domain.DOMAIN
import org.koin.android.ext.android.startKoin

class GameDealzApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(MAIN) + DOMAIN)
    }
}