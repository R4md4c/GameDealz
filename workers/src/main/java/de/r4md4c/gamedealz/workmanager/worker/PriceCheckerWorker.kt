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

package de.r4md4c.gamedealz.workmanager.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.r4md4c.commonproviders.notification.Notifier
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import de.r4md4c.gamedealz.domain.usecase.CheckPriceThresholdUseCase
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.SocketTimeoutException

internal class PriceCheckerWorker(
    appContext: Context,
    params: WorkerParameters,
    private val dispatchers: IDispatchers,
    private val notifier: Notifier<WatcheeNotificationModel>,
    private val getPriceThresholdUseCase: CheckPriceThresholdUseCase
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Timber.i("Starting PriceCheckerWorker")
        val result = kotlin.runCatching {
            withContext(dispatchers.IO) { getPriceThresholdUseCase() }
        }.onFailure {
            Timber.e(it, "Failure in PriceCheckerWorker")
        }.onSuccess {
            if (it.isNotEmpty()) {
                notifier.notify(it)
            }
        }

        return if (result.isSuccess) {
            Timber.i("PriceCheckerWorker finished successfully!")
            Result.success()
        } else {
            result.exceptionOrNull()?.takeIf { it.cause is SocketTimeoutException }?.let {
                Result.retry()
            } ?: Result.failure()
        }
    }
}
