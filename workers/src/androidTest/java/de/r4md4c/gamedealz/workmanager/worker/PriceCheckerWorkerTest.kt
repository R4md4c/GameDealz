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

import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.*
import de.r4md4c.commonproviders.notification.Notifier
import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.domain.model.*
import de.r4md4c.gamedealz.domain.usecase.CheckPriceThresholdUseCase
import de.r4md4c.gamedealz.test.TestDispatchers
import de.r4md4c.gamedealz.workmanager.WorkManagerJobsInitializer
import de.r4md4c.gamedealz.workmanager.factory.GameDealzWorkManagerFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class PriceCheckerWorkerTest {

    lateinit var testDriver: TestDriver

    lateinit var testWorkManager: WorkManager

    @Mock
    lateinit var notifier: Notifier<WatcheeNotificationModel>

    @Mock
    lateinit var checkPriceThresholdUseCase: CheckPriceThresholdUseCase

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        WorkManagerTestInitHelper.initializeTestWorkManager(
            InstrumentationRegistry.getInstrumentation().targetContext,
            Configuration.Builder()
                .setWorkerFactory(GameDealzWorkManagerFactory())
                .build()
        )
        testDriver = WorkManagerTestInitHelper.getTestDriver()
        testWorkManager = WorkManager.getInstance()

        startKoin(listOf(module {
            factory<IDispatchers> { TestDispatchers }
            factory { notifier }
            factory { checkPriceThresholdUseCase }
        }))
    }

    @After
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun doWorkNotifies_whenUseCase_ReturnsWatchees() {
        val mocks = (1..5)
            .map {
                WatcheeNotificationModel(
                    watcheeModel =
                    WatcheeModel(
                        it.toLong(), "$it", "$it", currentPrice = it.toFloat(), targetPrice = it.toFloat(),
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
        ArrangeBuilder()
            .withNotificationModels(mocks)
            .arrange()

        verify(notifier).notify(mocks)
    }

    @Test
    fun doWorkDoesNotNotify_whenUseCase_ReturnsEmptyWatchees() {
        ArrangeBuilder()
            .withNotificationModels(emptySet())
            .arrange()

        verify(notifier, never()).notify(any())
    }

    inner class ArrangeBuilder {

        fun withNotificationModels(watcheeModel: Set<WatcheeNotificationModel>) = apply {
            runBlocking {
                whenever(checkPriceThresholdUseCase.invoke(anyOrNull())).thenReturn(watcheeModel.toSet())
            }
        }

        fun arrange(): UUID {
            val sharedPreferencesProvider = mock<SharedPreferencesProvider> {
                on { priceCheckerPeriodicIntervalInHours } doReturn 6
            }

            return WorkManagerJobsInitializer(WorkManager.getInstance(), sharedPreferencesProvider).run {
                runBlocking {
                    init()
                }
                priceCheckerId.also {

                    testDriver.setAllConstraintsMet(it)
                    testDriver.setPeriodDelayMet(it)
                }
            }
        }
    }
}