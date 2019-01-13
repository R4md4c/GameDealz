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

package de.r4md4c.gamedealz.watchlist.shortcut

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class ShortcutCreatedBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val viewNotifier by inject<ViewNotifier>()

    private val resourceProvider by inject<ResourcesProvider>()

    override fun onReceive(context: Context?, intent: Intent?) {
        viewNotifier.notify(resourceProvider.getString(R.string.shortcut_created_successfully))
    }
}
