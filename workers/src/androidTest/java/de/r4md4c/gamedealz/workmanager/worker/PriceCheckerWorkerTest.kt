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
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.TestListenableWorkerBuilder
import com.nhaarman.mockitokotlin2.*
import de.r4md4c.commonproviders.notification.Notifier
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.domain.model.*
import de.r4md4c.gamedealz.domain.usecase.CheckPriceThresholdUseCase
import de.r4md4c.gamedealz.test.TestDispatchers
import de.r4md4c.gamedealz.workmanager.factory.GameDealzWorkManagerFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class PriceCheckerWorkerTest : AutoCloseKoinTest() {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Mock
    lateinit var notifier: Notifier<WatcheeNotificationModel>

    @Mock
    lateinit var checkPriceThresholdUseCase: CheckPriceThresholdUseCase

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        startKoin(listOf(module {
            factory<IDispatchers> { TestDispatchers }
            factory { notifier }
            factory { checkPriceThresholdUseCase }
        }))
    }

    @Test
    fun doWorkNotifies_whenUseCase_ReturnsWatchees() {
        val mocks = (1..5)
            .map {
                WatcheeNotificationModel(
                    watcheeModel =
                    WatcheeModel(
                        it.toLong(),
                        "$it",
                        "$it",
                        targetPrice = it.toFloat(),
                        lastFetchedStoreName = "",
                        lastFetchedPrice = it.toFloat(),
                        regionCode = "EU1", countryCode = "DE", currencyCode = "EU"
                    ),
                    priceModel = PriceModel(
                        it.toFloat(), it.toFloat(), 0, "",
                        ShopModel("shop$it", "name$it", ""), emptySet()
                    ),
                    currencyModel = CurrencyModel("EUR", "")
                )
            }
            .toSet()
        val worker = ArrangeBuilder()
            .withNotificationModels(mocks)
            .arrange()

        runBlocking {
            worker.doWork()
        }

        verify(notifier).notify(mocks)
    }

    @Test
    fun doWorkDoesNotNotify_whenUseCase_ReturnsEmptyWatchees() {
        val worker = ArrangeBuilder()
            .withNotificationModels(emptySet())
            .arrange()

        runBlocking {
            worker.doWork()
        }

        verify(notifier, never()).notify(any())
    }

    private inner class ArrangeBuilder {

        fun withNotificationModels(watcheeModel: Set<WatcheeNotificationModel>) = apply {
            runBlocking {
                whenever(checkPriceThresholdUseCase.invoke(anyOrNull())).thenReturn(watcheeModel.toSet())
            }
        }

        fun arrange(): PriceCheckerWorker =
            TestListenableWorkerBuilder.from<PriceCheckerWorker>(context, PriceCheckerWorker::class.java)
                .setWorkerFactory(GameDealzWorkManagerFactory())
                .build() as PriceCheckerWorker
    }
}
