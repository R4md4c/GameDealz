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
import de.r4md4c.gamedealz.data.repository.PriceAlertLocalDataSource
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.ShopModel
import de.r4md4c.gamedealz.domain.model.WatcheeModel
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class PriceAlertsHelperTest {

    private val priceAlertDataSource = mock<PriceAlertLocalDataSource>()

    private val dateProvider = mock<DateProvider>()

    private val priceAlertsHelper = PriceAlertsHelper(priceAlertDataSource, dateProvider)

    @Before
    fun beforeEach() {
        clearInvocations(priceAlertDataSource, dateProvider)
    }

    @Test
    fun `it should not invoke price alert repository when argument is empty`() {
        ArrangeBuilder()

        runBlocking { priceAlertsHelper.storeNotificationModels(emptyList()) }

        verifyNoInteractions(priceAlertDataSource)
    }

    @Test
    fun `it should save price alert to price repository`() {
        ArrangeBuilder()
            .withDate(1000)

        runBlocking { priceAlertsHelper.storeNotificationModels((1..5).map { notificationModel }) }

        verifyBlocking(priceAlertDataSource) {
            save((1..5).map {
                PriceAlert(
                    0, 1,
                    buyUrl = "http://google.com", storeName = "name2", dateCreated = 1
                )
            })
        }

    }

    @Test
    fun `it should convert millis to seconds`() {
        ArrangeBuilder()
            .withDate(2000)

        runBlocking { priceAlertsHelper.storeNotificationModels(listOf(notificationModel)) }

        verifyBlocking(priceAlertDataSource) {
            save(argThat { size == 1 && first().dateCreated == 2L })
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
        fun withDate(date: Long) = apply {
            whenever(dateProvider.timeInMillis()) doReturn date
        }
    }
}
