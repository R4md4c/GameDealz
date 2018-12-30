package de.r4md4c.gamedealz

import androidx.multidex.MultiDexApplication
import de.r4md4c.gamedealz.domain.DOMAIN
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class GameDealzApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin(this, listOf(MAIN) + DOMAIN)
    }
}