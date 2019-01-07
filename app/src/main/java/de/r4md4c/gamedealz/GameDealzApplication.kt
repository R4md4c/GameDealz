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
import androidx.multidex.MultiDexApplication
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.acra.AcraReportSenderFactory
import de.r4md4c.gamedealz.common.launchWithCatching
import de.r4md4c.gamedealz.domain.DOMAIN
import de.r4md4c.gamedealz.workmanager.WORK_MANAGER
import de.r4md4c.gamedealz.workmanager.WorkerJobsInitializer
import kotlinx.coroutines.GlobalScope
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

    private val workerJobsInitializer by inject<WorkerJobsInitializer>()

    private val dispatchers: IDispatchers by inject()

    override fun onCreate() {
        super.onCreate()
        val isDebug = BuildConfig.DEBUG

        if (isDebug) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin(this, listOf(MAIN) + DOMAIN + WORK_MANAGER, logger = if (isDebug) AndroidLogger() else EmptyLogger())
        initializeWorkManager()
    }

    private fun initializeWorkManager() {
        GlobalScope.launchWithCatching(dispatchers.Default, {
            workerJobsInitializer.init()
        }) {
            Timber.e(it, "Failed to init() WorkerJobsInitializer")
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ACRA.init(this)
    }
}