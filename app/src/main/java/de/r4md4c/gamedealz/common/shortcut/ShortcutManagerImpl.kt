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

package de.r4md4c.gamedealz.common.shortcut

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.di.ForApplication
import javax.inject.Inject

internal class ShortcutManagerImpl @Inject constructor(
    private val context: Context,
    @ForApplication private val resourcesProvider: ResourcesProvider
) : ShortcutManager {

    override fun addManageWatchlistShortcut() {
        if (!doesLauncherSupportsShortcuts()) {
            return
        }

        val info = ShortcutInfoCompat.Builder(context, MANAGE_WATCHLIST_SHORTCUT)
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_manage_watchlist_launcher))
            .setShortLabel(resourcesProvider.getString(R.string.watchlist))
            .setLongLabel(resourcesProvider.getString(R.string.title_manage_your_watchlist))
            .setIntent(intent())
            .build()

        ShortcutManagerCompat.requestPinShortcut(context, info, null)
    }

    override fun doesLauncherSupportsShortcuts(): Boolean =
        ShortcutManagerCompat.isRequestPinShortcutSupported(context)

    private fun intent(): Intent =
        NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.manageWatchlistFragment)
            .createTaskStackBuilder()
            .editIntentAt(0)!!
            .also { it.removeExtra(NavController.KEY_DEEP_LINK_INTENT) }

    private companion object {
        private const val MANAGE_WATCHLIST_SHORTCUT = "manage_watchlist_shortcut"
    }
}
