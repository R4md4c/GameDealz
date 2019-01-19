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

package de.r4md4c.gamedealz.domain.usecase.impl.internal

import de.r4md4c.commonproviders.date.DateProvider
import de.r4md4c.gamedealz.data.entity.PriceAlert
import de.r4md4c.gamedealz.data.repository.PriceAlertRepository
import de.r4md4c.gamedealz.domain.model.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class PriceAlertsHelperTest {

    private val priceAlertRepository = mockk<PriceAlertRepository>()

    private val dateProvider = mockk<DateProvider>()

    private val priceAlertsHelper = PriceAlertsHelper(priceAlertRepository, dateProvider)

    @Before
    fun beforeEach() {
        clearMocks(priceAlertRepository, dateProvider)
    }

    @Test
    fun `it should not invoke price alert repository when argument is empty`() {
        ArrangeBuilder()

        runBlocking { priceAlertsHelper.storeNotificationModels(emptyList()) }

        coVerify(exactly = 0) { priceAlertRepository.save(allAny()) }
        confirmVerified(priceAlertRepository)
    }

    @Test
    fun `it should save price alert to price repository`() {
        ArrangeBuilder()
            .withDate(1000)

        runBlocking { priceAlertsHelper.storeNotificationModels((1..5).map { notificationModel }) }

        coVerify {
            priceAlertRepository.save((1..5).map {
                PriceAlert(
                    0, 1,
                    buyUrl = "http://google.com", storeName = "name2", dateCreated = 1
                )
            })
        }
        confirmVerified(priceAlertRepository)
    }

    @Test
    fun `it should convert millis to seconds`() {
        ArrangeBuilder()
            .withDate(2000)

        runBlocking { priceAlertsHelper.storeNotificationModels(listOf(notificationModel)) }

        coVerify {
            priceAlertRepository.save(match { it.size == 1 && it.first().dateCreated == 2L })
            dateProvider.timeInMillis()
        }
    }

    private val notificationModel = WatcheeNotificationModel(
        watcheeModel =
        WatcheeModel(
            1, "1", "title1", targetPrice = 10f, lastFetchedStoreName = "store", lastFetchedPrice = 10f,
            regionCode = "EU1", countryCode = "DE", currencyCode = "EU"
        ),
        priceModel = PriceModel(
            10f, 10f, 0, "http://google.com",
            ShopModel("shop1", "name2", ""), emptySet()
        ),
        currencyModel = CurrencyModel("EUR", "")
    )

    inner class ArrangeBuilder {
        init {
            coEvery { priceAlertRepository.save(any()) } just Runs
        }

        fun withDate(date: Long) = apply {
            every { dateProvider.timeInMillis() } returns date
        }
    }
}
