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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.SparseArray
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.core.util.forEach
import de.r4md4c.commonproviders.notification.Notifier
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.WatcheeModel

internal class WatcheesPushNotifier(
    private val context: Context,
    private val resourcesProvider: ResourcesProvider
) : Notifier<WatcheeModel> {

    private val notificationManager by lazy {
        NotificationManagerCompat.from(context).also { createNotificationChannel() }
    }

    override fun notify(data: Collection<WatcheeModel>) {
        val notifications = data.notificationsFromWatchees().takeIf { it.size() > 0 } ?: return

        buildSummaryNotification(notifications.size()).also {
            notifications.put(notifications.size() + 1, it)
        }

        notifications.forEach { key, value ->
            notificationManager.notify(key, value)
        }
    }

    private fun Collection<WatcheeModel>.notificationsFromWatchees(): SparseArray<Notification> {
        val notificationsSparseArray = SparseArray<Notification>()
        forEach { watcheeModel ->
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(resourcesProvider.getString(R.string.watchlist_notification_title))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentText(
                    resourcesProvider.getString(
                        R.string.watchlist_notification_content,
                        watcheeModel.title,
                        watcheeModel.currentPrice.toString(),
                        "Steam"
                    )
                )
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setGroup(GROUP_KEY)
                .also {
                    notificationsSparseArray.put(watcheeModel.id!!.toInt(), it.build())
                }
        }
        return notificationsSparseArray
    }

    private fun buildSummaryNotification(notificationsTotalSize: Int) =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(
                resourcesProvider.getString(
                    R.string.watchlist_notification_summary,
                    notificationsTotalSize.toString()
                )
            )
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = resourcesProvider.getString(R.string.watchlist_channel_name)
            val descriptionText = resourcesProvider.getString(R.string.watchlist_channel_description)
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = descriptionText
            context.getSystemService<NotificationManager>()?.createNotificationChannel(channel)
        }
    }

    private companion object {
        private const val GROUP_KEY = "watchlist_notification_group"
        private const val CHANNEL_ID = "watchlist_notification_channel_id"
    }
}
