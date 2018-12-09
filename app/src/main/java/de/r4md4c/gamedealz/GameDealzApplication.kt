package de.r4md4c.gamedealz

import android.app.Application
import de.r4md4c.gamedealz.domain.DOMAIN
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class GameDealzApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin(this, listOf(MAIN) + DOMAIN)
    }
}