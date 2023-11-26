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

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.SparseArray
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import androidx.navigation.NavDeepLinkBuilder
import de.r4md4c.commonproviders.notification.Notifier
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.di.ForApplication
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import de.r4md4c.gamedealz.domain.model.formatCurrency
import de.r4md4c.gamedealz.feature.watchlist.R
import java.util.*
import javax.inject.Inject

class WatcheesPushNotifier @Inject constructor(
    private val context: Context,
    @ForApplication private val resourcesProvider: ResourcesProvider
) : Notifier<WatcheeNotificationModel> {

    private val notificationManager by lazy {
        NotificationManagerCompat.from(context).apply { createNotificationChannel() }
    }

    override fun notify(data: Collection<WatcheeNotificationModel>) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            return
        }

        val notifications = data.notificationsFromWatchees().takeIf { it.size() > 0 } ?: return

        buildSummaryNotification(notifications.size())?.also {
            notifications.put(SUMMARY_ID, it)
        }

        notifications.forEach { key, value ->
            notificationManager.notify(key, value)
        }
    }

    private fun Collection<WatcheeNotificationModel>.notificationsFromWatchees(): SparseArray<Notification> {
        val notificationsSparseArray = SparseArray<Notification>()
        forEachIndexed { index, notificationModel ->
            val watcheeModel = notificationModel.watcheeModel
            val priceModel = notificationModel.priceModel
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setContentTitle(resourcesProvider.getString(R.string.watchlist_notification_title))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentText(
                    resourcesProvider.getString(
                        R.string.watchlist_notification_content,
                        notificationModel.watcheeModel.title,
                        priceModel.newPrice.formatCurrency(notificationModel.currencyModel) ?: "",
                        priceModel.shop.name
                    )
                )
                .apply {
                    // Set defaults for a single notification.
                    if (index == 0) {
                        setDefaults(NotificationCompat.DEFAULT_ALL)
                    }
                }
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setGroup(GROUP_KEY)
                .setAutoCancel(true)
                .setContentIntent(notificationModel.toDetailsPendingIntent())
                .addAction(
                    0,
                    resourcesProvider.getString(R.string.check_on, priceModel.shop.name)
                        .capitalize(),
                    notificationModel.toBuyUrlPendingIntent()
                )
                .also {
                    notificationsSparseArray.put(watcheeModel.id!!.toInt(), it.build())
                }
        }
        return notificationsSparseArray
    }

    private fun buildSummaryNotification(notificationsTotalSize: Int): Notification? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || notificationsTotalSize <= 1) {
            return null
        }
        return NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setContentTitle(
                resourcesProvider.getString(
                    R.string.watchlist_notification_summary,
                    notificationsTotalSize.toString()
                )
            )
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setContentIntent(summaryIntent())
            .build()
    }

    private fun NotificationManagerCompat.createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = resourcesProvider.getString(R.string.watchlist_channel_name)
            val descriptionText =
                resourcesProvider.getString(R.string.watchlist_channel_description)
            val channel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = descriptionText
            createNotificationChannel(channel)
        }
    }

    private fun WatcheeNotificationModel.toBuyUrlPendingIntent(): PendingIntent =
        NotificationsBroadcastReceiver.toBuyUrlIntent(context, this)

    private fun WatcheeNotificationModel.toDetailsPendingIntent(): PendingIntent =
        NotificationsBroadcastReceiver.toPendingIntent(context, this)

    private fun summaryIntent() =
        NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.manageWatchlistFragment)
            .createTaskStackBuilder()
            .getPendingIntent(
                UUID.randomUUID().hashCode(),
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

    private companion object {
        private const val SUMMARY_ID = Integer.MAX_VALUE - 1
        private const val GROUP_KEY = "watchlist_notification_group"
        private const val CHANNEL_ID = "watchlist_notification_channel_id"
    }
}
