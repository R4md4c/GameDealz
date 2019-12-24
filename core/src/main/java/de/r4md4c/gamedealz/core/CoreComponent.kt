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

package de.r4md4c.gamedealz.core

import android.content.Context
import dagger.Subcomponent
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.notification.Notifier
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.auth.di.AuthComponent
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.di.ForApplication
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import okhttp3.OkHttpClient

@Subcomponent
interface CoreComponent : UseCaseComponent {

    val authComponent: AuthComponent

    val okHttpClient: OkHttpClient

    val applicationContext: Context

    val dispatchers: IDispatchers

    val dateFormatter: DateFormatter

    @ForApplication
    fun resourcesProvider(): ResourcesProvider

    val watchlistPushNotifier: Notifier<@JvmSuppressWildcards WatcheeNotificationModel>
}
