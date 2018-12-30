package de.r4md4c.gamedealz

import androidx.multidex.MultiDexApplication
import de.r4md4c.gamedealz.domain.DOMAIN
import org.koin.android.ext.android.startKoin
import org.koin.android.logger.AndroidLogger
import org.koin.log.EmptyLogger
import timber.log.Timber

class GameDealzApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        val isDebug = BuildConfig.DEBUG

        if (isDebug) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin(this, listOf(MAIN) + DOMAIN, logger = if (isDebug) AndroidLogger() else EmptyLogger())
    }
}