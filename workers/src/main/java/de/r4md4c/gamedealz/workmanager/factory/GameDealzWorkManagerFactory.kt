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

package de.r4md4c.gamedealz.workmanager.factory

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import de.r4md4c.gamedealz.workmanager.worker.PriceCheckerWorker
import org.koin.standalone.KoinComponent
import org.koin.standalone.get

internal class GameDealzWorkManagerFactory : WorkerFactory(), KoinComponent {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? =
        when (workerClassName) {
            PriceCheckerWorker::class.java.name -> PriceCheckerWorker(appContext, workerParameters, get(), get(), get())
            else -> throw IllegalArgumentException("$workerClassName is not supported.")
        }
}
