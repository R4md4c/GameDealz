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

package de.r4md4c.gamedealz.common.notifications

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.common.truth.Truth.assertThat
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.WatcheeModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.get
import org.koin.standalone.inject

class WatcheesPushNotifierIntegrationTest : KoinComponent {

    private lateinit var uiDevice: UiDevice

    private lateinit var watcheesPushNotifier: WatcheesPushNotifier

    private val resourcesProvider by inject<ResourcesProvider>()

    @Before
    fun beforeEach() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        watcheesPushNotifier = WatcheesPushNotifier(
            InstrumentationRegistry.getInstrumentation().targetContext,
            get()
        )
    }

    @Test
    fun testShowingNotification_showsSummary() {
        clearAllNotifications()
        val data = (1..10).map {
            WatcheeModel(
                id = it.toLong(),
                plainId = "plainId$it",
                title = "title$it",
                dateAdded = it.toLong(),
                lastCheckDate = it.toLong(),
                targetPrice = it.toFloat(),
                currentPrice = it.toFloat(),
                regionCode = "EU1",
                countryCode = "DE",
                currencyCode = "EUR"
            )
        }

        watcheesPushNotifier.notify(data)

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.textStartsWith(resourcesProvider.getString(R.string.app_name))), TIMEOUT)
        uiDevice.findObject(
            By.text(
                resourcesProvider.getString(
                    R.string.watchlist_notification_title,
                    data.size.toString()
                )
            )
        ).also {
            assertThat(it).isNotNull()
        }
    }

    @After
    fun afterEach() {
        stopKoin()
    }

    private fun clearAllNotifications() {
        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.textStartsWith(resourcesProvider.getString(R.string.app_name))), TIMEOUT)
        uiDevice.findObject(By.desc("Clear all notifications."))?.let { it.click() }
    }

    private companion object {
        private const val TIMEOUT = 5000L
    }
}