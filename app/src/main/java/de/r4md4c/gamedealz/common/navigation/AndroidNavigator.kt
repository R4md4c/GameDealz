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

package de.r4md4c.gamedealz.common.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import de.r4md4c.gamedealz.common.deepllink.DeepLinks
import de.r4md4c.gamedealz.deals.DealsFragmentDirections
import de.r4md4c.gamedealz.detail.DetailsFragmentDirections

class AndroidNavigator(
    private val context: Context,
    private val navController: NavController
) : Navigator {

    override fun navigate(uri: String, extras: Parcelable?) {
        val navDirection: NavDirections? = getNavDirections(Uri.parse(uri), extras)
        navDirection?.let { navController.navigate(navDirection) }
    }

    override fun navigateUp() = navController.navigateUp()

    override fun navigateToUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun getNavDirections(parsedUri: Uri, extras: Parcelable?): NavDirections? =
        when (parsedUri.pathSegments[0]) {
            DeepLinks.PATH_SEARCH -> {
                val searchQuery =
                    requireNotNull(parsedUri.getQueryParameter(DeepLinks.QUERY_SEARCH_TERM)) { "search term is required." }
                DealsFragmentDirections.actionDealsFragmentToSearchFragment(searchQuery)
            }
            DeepLinks.PATH_DETAIL -> {
                val title =
                    requireNotNull(parsedUri.getQueryParameter(DeepLinks.QUERY_TITLE)) { "title is required to open details" }
                val plainId =
                    requireNotNull(parsedUri.getQueryParameter(DeepLinks.QUERY_PLAIN_ID)) { "plainId is required to open details" }
                val buyUrl =
                    requireNotNull(parsedUri.getQueryParameter(DeepLinks.QUERY_BUY_URL)) { "buyUrl is required to open details." }

                DetailsFragmentDirections.actionGlobalGameDetailFragment(plainId, title, buyUrl)
            }
            else -> {
                throw IllegalArgumentException("Unknown Deeplink: $parsedUri")
            }
        }

}