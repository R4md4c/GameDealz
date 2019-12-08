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

package de.r4md4c.gamedealz.feature.watchlist.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import de.r4md4c.commonproviders.res.AndroidResourcesProvider
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.ShopModel
import de.r4md4c.gamedealz.domain.model.WatcheeModel
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import de.r4md4c.gamedealz.feature.watchlist.R
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class WatcheesPushNotifierIntegrationTest {

    private lateinit var uiDevice: UiDevice

    private lateinit var watcheesPushNotifier: WatcheesPushNotifier

    private val targetContext
        get() = ApplicationProvider.getApplicationContext<Context>()

    private val resourcesProvider = AndroidResourcesProvider(targetContext)

    @Before
    fun beforeEach() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        watcheesPushNotifier =
            WatcheesPushNotifier(
                InstrumentationRegistry.getInstrumentation().targetContext,
                resourcesProvider
            )
    }

    @Test
    fun showingMore10Notifications_showsSummary() {
        clearAllNotifications()
        val data = (1..10).map {
            WatcheeNotificationModel(
                watcheeModel = WatcheeModel(
                    id = it.toLong(),
                    plainId = "plainId$it",
                    title = "title$it",
                    dateAdded = it.toLong(),
                    lastCheckDate = it.toLong(),
                    lastFetchedPrice = it.toFloat(),
                    lastFetchedStoreName = "store",
                    targetPrice = it.toFloat(),
                    regionCode = "EU1",
                    countryCode = "DE",
                    currencyCode = "EUR"
                ),
                priceModel = PriceModel(
                    10f,
                    10f,
                    0,
                    "http://google.com",
                    ShopModel("", "Steam", ""),
                    emptySet()
                ),
                currencyModel = CurrencyModel(
                    "EUR",
                    ""
                )
            )
        }

        watcheesPushNotifier.notify(data)

        waitForNotification()
        uiDevice.findObject(
            By.text(
                resourcesProvider.getString(
                    R.string.watchlist_notification_title,
                    data.size.toString()
                )
            )
        ).also {
            Assertions.assertThat(it).isNotNull()
        }
    }

    @Test
    fun showing1Notification_clickOpensDetails() {
        clearAllNotifications()
        val data = WatcheeNotificationModel(
            watcheeModel = WatcheeModel(
                id = 1,
                plainId = "plainId1",
                title = "title$1",
                dateAdded = 1,
                lastCheckDate = 1,
                lastFetchedPrice = 1f,
                lastFetchedStoreName = "store",
                targetPrice = 1f,
                regionCode = "EU1",
                countryCode = "DE",
                currencyCode = "EUR"
            ),
            priceModel = PriceModel(
                10f,
                10f,
                0,
                "http://google.com",
                ShopModel("", "Steam", ""),
                emptySet()
            ),
            currencyModel = CurrencyModel(
                "EUR",
                ""
            )
        )

        watcheesPushNotifier.notify(setOf(data))

        waitForNotification()
        uiDevice.findObject(
            By.textStartsWith("Price Alert")
        ).also {
            Assertions.assertThat(it).isNotNull()
            it.click()
        }
    }

    private fun clearAllNotifications() {
        uiDevice.openNotification()
        uiDevice.wait(
            Until.hasObject(By.textStartsWith(resourcesProvider.getString(R.string.app_name))),
            TIMEOUT
        )
        uiDevice.findObject(By.desc("Clear all notifications."))?.let { it.click() }
    }

    private fun waitForNotification() {
        uiDevice.openNotification()
        uiDevice.wait(
            Until.hasObject(By.textStartsWith(resourcesProvider.getString(R.string.app_name))),
            TIMEOUT
        )
    }

    private companion object {
        private const val TIMEOUT = 5000L
    }
}
