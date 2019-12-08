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

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.ShopModel
import de.r4md4c.gamedealz.domain.model.WatcheeModel
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import de.r4md4c.gamedealz.domain.usecase.GetAlertsCountUseCase
import de.r4md4c.gamedealz.domain.usecase.MarkNotificationAsReadUseCase
import de.r4md4c.gamedealz.test.TestDispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowNotificationManager

@RunWith(RobolectricTestRunner::class)
@Config(application = FakeBroadcastReceiverApplication::class)
class NotificationsBroadcastReceiverTest {

    @Mock
    private lateinit var markNotificationAsReadUseCase: MarkNotificationAsReadUseCase

    @Mock
    private lateinit var alertsCountUseCase: GetAlertsCountUseCase

    private lateinit var shadowNotificationManager: ShadowNotificationManager

    private lateinit var shadowApplication: ShadowApplication

    private lateinit var notificationsBroadcastReceiver: NotificationsBroadcastReceiver

    private val applicationContext
        get() = ApplicationProvider.getApplicationContext<FakeBroadcastReceiverApplication>()

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)
        shadowNotificationManager =
            Shadows.shadowOf(applicationContext.getSystemService<NotificationManager>())
        shadowApplication =
            Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())

        val application =
            ApplicationProvider.getApplicationContext<FakeBroadcastReceiverApplication>()
        whenever(application.mockCoreComponent.dispatchers).thenReturn(TestDispatchers)
        whenever(application.mockCoreComponent.markNotificationAsReadUseCase).thenReturn(this.markNotificationAsReadUseCase)
        whenever(application.mockCoreComponent.getAlertsCountUseCase).thenReturn(this.alertsCountUseCase)

        notificationsBroadcastReceiver =
            NotificationsBroadcastReceiver()
    }

    @After
    fun afterEach() {
        val application =
            ApplicationProvider.getApplicationContext<FakeBroadcastReceiverApplication>()
        reset(application.mockCoreComponent)
    }

    @Test
    fun `onReceive invoke mark notification as read`() {
        val builder = ArrangeBuilder()
            .withExtraModel(NOTIFICATION_MODEL)

        notificationsBroadcastReceiver.onReceive(applicationContext, builder.arrange())

        runBlocking {
            verify(markNotificationAsReadUseCase)
                .invoke(
                    TypeParameter(NOTIFICATION_MODEL.watcheeModel)
                )
        }
    }

    @Test
    fun `onReceive does not mark notification as read when model is null`() {
        val builder = ArrangeBuilder()

        notificationsBroadcastReceiver.onReceive(applicationContext, builder.arrange())

        runBlocking {
            verify(
                markNotificationAsReadUseCase,
                never()
            ).invoke(
                TypeParameter(
                    NOTIFICATION_MODEL.watcheeModel
                )
            )
        }
    }

    @Test
    fun `onReceive with game details actions opens game details screen`() {
        val builder = ArrangeBuilder()
            .withExtraModel(NOTIFICATION_MODEL)
            .withViewGameDetailsAction()

        // Using activity because of this issue https://github.com/robolectric/robolectric/issues/3953
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        notificationsBroadcastReceiver.onReceive(activity, builder.arrange())

        Assertions.assertThat(shadowApplication.nextStartedActivity)
            .isNotNull()
    }

    @Test
    fun `onReceive with buy url invokes intent with ACTION_VIEW`() {
        val builder = ArrangeBuilder()
            .withExtraModel(NOTIFICATION_MODEL)
            .withBuyUrlAction()

        notificationsBroadcastReceiver.onReceive(applicationContext, builder.arrange())

        shadowApplication.nextStartedActivity.apply {
            Assertions.assertThat(action)
                .isEqualTo(Intent.ACTION_VIEW)
            Assertions.assertThat(data)
                .isEqualTo(Uri.parse("http://google.com"))
            Assertions.assertThat(flags)
                .isEqualTo(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    @Test
    fun `onReceive with buy url cancels notification`() {
        val builder = ArrangeBuilder()
            .withExtraModel(NOTIFICATION_MODEL.copy(NOTIFICATION_MODEL.watcheeModel.copy(id = 5)))
            .withBuyUrlAction()
            .withNotificationWithId(5)

        notificationsBroadcastReceiver.onReceive(applicationContext, builder.arrange())

        Assertions.assertThat(shadowNotificationManager.activeNotifications)
            .isEmpty()
    }

    @Test
    fun `onReceive should cancel all notifications when the article count becomes 0`() {
        val builder = ArrangeBuilder()
            .withExtraModel(NOTIFICATION_MODEL.copy(NOTIFICATION_MODEL.watcheeModel.copy(id = 5)))
            .withBuyUrlAction()
            .withNotificationWithId(5)
            .withNotificationWithId(6)
            .withAlertsCountUseCase(0)

        notificationsBroadcastReceiver.onReceive(applicationContext, builder.arrange())

        Assertions.assertThat(shadowNotificationManager.activeNotifications)
            .isEmpty()
    }

    private companion object {
        val NOTIFICATION_MODEL =
            WatcheeNotificationModel(
                watcheeModel =
                WatcheeModel(
                    1,
                    "1",
                    "title1",
                    targetPrice = 10f,
                    lastFetchedPrice = 10f,
                    lastFetchedStoreName = "shop1",
                    regionCode = "EU1",
                    countryCode = "DE",
                    currencyCode = "EU"
                ),
                priceModel = PriceModel(
                    10f, 10f, 0, "http://google.com",
                    ShopModel(
                        "shop1",
                        "name2",
                        ""
                    ), emptySet()
                ),
                currencyModel = CurrencyModel(
                    "EUR",
                    ""
                )
            )
    }

    inner class ArrangeBuilder {
        private val intent = Intent()

        init {
            withAlertsCountUseCase(-1)
        }

        fun withExtraModel(watcheeNotificationModel: WatcheeNotificationModel) = apply {
            intent.putExtra("extra_notification_model", watcheeNotificationModel)
        }

        fun withViewGameDetailsAction() = apply {
            intent.action = "${Intent.ACTION_VIEW}.game_details"
        }

        fun withBuyUrlAction() = apply {
            intent.action = "${Intent.ACTION_VIEW}.buyUrl"
        }

        fun withNotificationWithId(id: Int) = apply {
            NotificationManagerCompat.from(applicationContext)
                .apply {
                    notify(
                        id,
                        NotificationCompat.Builder(
                            applicationContext,
                            NotificationChannel.DEFAULT_CHANNEL_ID
                        ).build()
                    )
                }
            Assertions.assertThat(shadowNotificationManager.activeNotifications)
                .isNotEmpty()
        }

        fun withAlertsCountUseCase(count: Int) = apply {
            runBlocking {
                whenever(alertsCountUseCase())
                    .thenReturn(flowOf(count))
            }
        }

        fun arrange() = intent
    }
}