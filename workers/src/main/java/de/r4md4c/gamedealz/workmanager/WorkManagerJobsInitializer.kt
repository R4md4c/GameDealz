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

package de.r4md4c.gamedealz.workmanager

import androidx.annotation.VisibleForTesting
import androidx.work.*
import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.gamedealz.workmanager.worker.PriceCheckerWorker
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

internal class WorkManagerJobsInitializer(
    private val workManager: WorkManager,
    private val preferenences: SharedPreferencesProvider
) : PricesCheckerWorker {

    @VisibleForTesting
    internal var priceCheckerId: UUID by Delegates.notNull()

    override suspend fun schedulePeriodically() {
        if (enqueuePriceChecker().await() == Operation.SUCCESS) {
            Timber.i("Enqueuing Price Checker Success")
        }
    }

    private fun enqueuePriceChecker(): Operation =
        workManager.enqueueUniquePeriodicWork(
            PRICE_CHECKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<PriceCheckerWorker>(
                preferenences.priceCheckerPeriodicIntervalInHours.toLong(),
                TimeUnit.HOURS,
                FLEX_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            ).apply {
                setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            }.build().apply {
                priceCheckerId = id
            }
        )

    private companion object {
        private const val PRICE_CHECKER_UNIQUE_NAME = "price_checker_unique"
        private const val FLEX_INTERVAL_MINUTES = 30L
    }
}
