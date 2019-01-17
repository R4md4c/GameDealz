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
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.*
import de.r4md4c.gamedealz.domain.usecase.MarkNotificationAsReadUseCase
import de.r4md4c.gamedealz.test.TestDispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.loadKoinModules
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.test.KoinTest
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowNotificationManager

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class NotificationsBroadcastReceiverTest : KoinTest {

    @Mock
    private lateinit var markNotificationAsReadUseCase: MarkNotificationAsReadUseCase

    private lateinit var shadowNotificationManager: ShadowNotificationManager

    private lateinit var shadowApplication: ShadowApplication

    private lateinit var notificationsBroadcastReceiver: NotificationsBroadcastReceiver

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)
        shadowNotificationManager =
                Shadows.shadowOf(RuntimeEnvironment.systemContext.getSystemService<NotificationManager>())
        shadowApplication = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())

        notificationsBroadcastReceiver = NotificationsBroadcastReceiver()
        loadKoinModules(listOf(module {
            single<IDispatchers> { TestDispatchers }
            single { markNotificationAsReadUseCase }
        }))
    }

    @Test
    fun `onReceive invoke mark notification as read`() {
        val builder = ArrangeBuilder()
            .withExtraModel(NOTIFICATION_MODEL)

        notificationsBroadcastReceiver.onReceive(RuntimeEnvironment.systemContext, builder.arrange())

        runBlocking {
            verify(markNotificationAsReadUseCase).invoke(TypeParameter(NOTIFICATION_MODEL.watcheeModel))
        }
    }

    @Test
    fun `onReceive does not mark notification as read when model is null`() {
        val builder = ArrangeBuilder()

        notificationsBroadcastReceiver.onReceive(RuntimeEnvironment.systemContext, builder.arrange())

        runBlocking {
            verify(markNotificationAsReadUseCase, never()).invoke(TypeParameter(NOTIFICATION_MODEL.watcheeModel))
        }
    }

    @Test
    fun `onReceive with game details actions opens game details screen`() {
        val builder = ArrangeBuilder()
            .withExtraModel(NOTIFICATION_MODEL)
            .withViewGameDetailsAction()

        notificationsBroadcastReceiver.onReceive(
            Robolectric.buildActivity(Activity::class.java).get(),
            builder.arrange()
        )

        assertThat(shadowApplication.nextStartedActivity).isNotNull()
    }

    @Test
    fun `onReceive with buy url invokes intent with ACTION_VIEW`() {
        val builder = ArrangeBuilder()
            .withExtraModel(NOTIFICATION_MODEL)
            .withBuyUrlAction()

        notificationsBroadcastReceiver.onReceive(RuntimeEnvironment.systemContext, builder.arrange())

        shadowApplication.nextStartedActivity.apply {
            assertThat(action).isEqualTo(Intent.ACTION_VIEW)
            assertThat(data).isEquivalentAccordingToCompareTo(Uri.parse("http://google.com"))
            assertThat(flags).isEqualTo(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    @Test
    fun `onReceive with buy url cancels notification`() {
        val builder = ArrangeBuilder()
            .withExtraModel(NOTIFICATION_MODEL.copy(NOTIFICATION_MODEL.watcheeModel.copy(id = 5)))
            .withBuyUrlAction()
            .withNotificationWithId(5)

        notificationsBroadcastReceiver.onReceive(RuntimeEnvironment.systemContext, builder.arrange())

        assertThat(shadowNotificationManager.activeNotifications).isEmpty()
    }

    @After
    fun afterEach() {
        stopKoin()
    }

    private companion object {
        val NOTIFICATION_MODEL = WatcheeNotificationModel(
            watcheeModel =
            WatcheeModel(
                1, "1", "title1", targetPrice = 10f, lastFetchedPrice = 10f, lastFetchedStoreName = "shop1",
                regionCode = "EU1", countryCode = "DE", currencyCode = "EU"
            ),
            priceModel = PriceModel(
                10f, 10f, 0, "http://google.com",
                ShopModel("shop1", "name2", ""), emptySet()
            ),
            currencyModel = CurrencyModel("EUR", "")
        )
    }

    inner class ArrangeBuilder {
        private val intent = Intent()

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
            NotificationManagerCompat.from(RuntimeEnvironment.systemContext).apply {
                notify(
                    id,
                    NotificationCompat.Builder(
                        RuntimeEnvironment.systemContext,
                        NotificationChannel.DEFAULT_CHANNEL_ID
                    ).build()
                )
            }
            assertThat(shadowNotificationManager.activeNotifications).isNotEmpty()
        }

        fun arrange() = intent
    }
}