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

package de.r4md4c.gamedealz.di

import dagger.Binds
import dagger.Module
import de.r4md4c.commonproviders.notification.Notifier
import de.r4md4c.gamedealz.common.notifications.ToastViewNotifier
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import de.r4md4c.gamedealz.common.notifications.WatcheesPushNotifier
import de.r4md4c.gamedealz.common.shortcut.ShortcutManager
import de.r4md4c.gamedealz.common.shortcut.ShortcutManagerImpl
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel

@Module
internal abstract class ApplicationBindsModule {

    @Binds
    abstract fun bindsToastViewNotifier(it: ToastViewNotifier): ViewNotifier

    @Binds
    abstract fun bindsShortcutManager(it: ShortcutManagerImpl): ShortcutManager

    @Binds
    abstract fun bindsWatcheesPushNotifier(it: WatcheesPushNotifier): Notifier<WatcheeNotificationModel>
}
