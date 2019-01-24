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

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.detail.DetailsFragmentArgs
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import de.r4md4c.gamedealz.domain.usecase.GetAlertsCountUseCase
import de.r4md4c.gamedealz.domain.usecase.MarkNotificationAsReadUseCase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.firstOrNull
import kotlinx.coroutines.launch
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

/**
 * Provides an interceptor when clicking on notification, so that we can mark notifications as read before opening
 * the required activity.
 */
class NotificationsBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase by inject()

    private val activeAlertsCountUseCase: GetAlertsCountUseCase by inject()

    private val dispatchers: IDispatchers by inject()

    override fun onReceive(context: Context, intent: Intent) {
        GlobalScope.launch(dispatchers.Default) {
            val notificationManager = NotificationManagerCompat.from(context)

            val notificationModel = intent.getParcelableExtra<WatcheeNotificationModel>(EXTRA_MODEL) ?: return@launch
            markNotificationAsReadUseCase(TypeParameter(notificationModel.watcheeModel))
            val alertsCount = activeAlertsCountUseCase().firstOrNull() ?: -1

            if (intent.action == ACTION_VIEW_GAME_DETAILS) {
                notificationModel.toDetailsPendingIntent(context)?.send()
            } else if (intent.action == ACTION_VIEW_BUY_URL) {
                notificationManager.cancel(notificationModel.watcheeModel.id!!.toInt())
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(notificationModel.priceModel.url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }

            if (alertsCount == 0) {
                notificationManager.cancelAll()
            }
        }
    }

    private fun WatcheeNotificationModel.toDetailsPendingIntent(context: Context): PendingIntent? =
        NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.gameDetailFragment)
            .setArguments(toBundle())
            .createPendingIntent()

    private fun WatcheeNotificationModel.toBundle(): Bundle =
        DetailsFragmentArgs.Builder(watcheeModel.plainId, watcheeModel.title, priceModel.url)
            .build()
            .toBundle()

    companion object {

        fun toBuyUrlIntent(context: Context, notificationModel: WatcheeNotificationModel): PendingIntent =
            PendingIntent.getBroadcast(
                context, notificationModel.watcheeModel.id!!.toInt(),
                notificationModel.buyIntent(context), PendingIntent.FLAG_UPDATE_CURRENT
            )

        fun toPendingIntent(context: Context, notificationModel: WatcheeNotificationModel): PendingIntent =
            PendingIntent.getBroadcast(
                context, notificationModel.watcheeModel.id!!.toInt(),
                notificationModel.intent(context), PendingIntent.FLAG_UPDATE_CURRENT
            )

        private fun WatcheeNotificationModel.intent(context: Context): Intent =
            Intent(context, NotificationsBroadcastReceiver::class.java).also {
                it.action = ACTION_VIEW_GAME_DETAILS
                it.putExtra(EXTRA_MODEL, this)
            }

        private fun WatcheeNotificationModel.buyIntent(context: Context): Intent =
            Intent(context, NotificationsBroadcastReceiver::class.java).also {
                it.action = ACTION_VIEW_BUY_URL
                it.putExtra(EXTRA_MODEL, this)
            }

        private const val EXTRA_MODEL = "extra_notification_model"
        private const val ACTION_VIEW_GAME_DETAILS = "${Intent.ACTION_VIEW}.game_details"
        private const val ACTION_VIEW_BUY_URL = "${Intent.ACTION_VIEW}.buyUrl"
    }
}
