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

package de.r4md4c.gamedealz

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import de.r4md4c.gamedealz.common.acra.AcraReportSenderFactory
import de.r4md4c.gamedealz.domain.DOMAIN
import de.r4md4c.gamedealz.workmanager.PricesCheckerWorker
import de.r4md4c.gamedealz.workmanager.WORK_MANAGER
import kotlinx.coroutines.runBlocking
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.startKoin
import org.koin.android.logger.AndroidLogger
import org.koin.log.EmptyLogger
import timber.log.Timber

@AcraCore(
    reportSenderFactoryClasses = [AcraReportSenderFactory::class],
    buildConfigClass = BuildConfig::class
)
class GameDealzApplication : MultiDexApplication() {

    private val pricesCheckerWorker by inject<PricesCheckerWorker>()

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        val isDebug = BuildConfig.DEBUG

        if (isDebug) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin(this, listOf(MAIN) + DOMAIN + WORK_MANAGER, logger = if (isDebug) AndroidLogger() else EmptyLogger())
        initializeWorkManager()
    }

    private fun initializeWorkManager() {
        kotlin.runCatching {
            runBlocking {
                pricesCheckerWorker.schedulePeriodically()
            }
        }.onFailure {
            Timber.e(it, "Failed to init() WorkerJobsInitializer")
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ACRA.init(this)
    }
}