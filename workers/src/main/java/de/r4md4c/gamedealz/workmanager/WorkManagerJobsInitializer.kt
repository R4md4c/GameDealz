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
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.gamedealz.workmanager.worker.PriceCheckerWorker
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.properties.Delegates

internal class WorkManagerJobsInitializer @Inject constructor(
    private val workManager: WorkManager,
    private val preferences: SharedPreferencesProvider
) : PricesCheckerWorker {

    @VisibleForTesting
    internal var priceCheckerId: UUID by Delegates.notNull()

    override suspend fun schedulePeriodically() {
        enqueuePriceChecker().await()
    }

    private fun enqueuePriceChecker(): Operation =
        workManager.enqueueUniquePeriodicWork(
            PRICE_CHECKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<PriceCheckerWorker>(
                preferences.priceCheckerPeriodicIntervalInHours.toLong(),
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
        private const val FLEX_INTERVAL_MINUTES = 30L
        private const val PRICE_CHECKER_UNIQUE_NAME = "price_checker_unique"
    }
}
