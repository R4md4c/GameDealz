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

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import de.r4md4c.gamedealz.common.deepllink.DeepLinks
import de.r4md4c.gamedealz.detail.DetailsFragmentDirections
import de.r4md4c.gamedealz.feature.deals.DealsFragmentDirections
import javax.inject.Inject

/**
 * We're doing lazy, because we inject before super.onCreate, and at that the time the view
 * isn't ready yet, to get the NavController from.
 */
class AndroidNavigator @Inject constructor(
    private val fragmentActivity: FragmentActivity,
    private val navController: dagger.Lazy<NavController>
) : Navigator {

    override fun navigate(uri: String, extras: Parcelable?) {
        val navDirection: NavDirections? = getNavDirections(Uri.parse(uri), extras)
        navDirection?.let { navController.get().navigate(navDirection) }
    }

    override fun navigateUp() = navController.get().navigateUp()

    override fun navigateToUrl(url: String) {
        fragmentActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun getNavDirections(parsedUri: Uri, extras: Parcelable?): NavDirections? =
        when (parsedUri.pathSegments[0]) {
            DeepLinks.PATH_SEARCH -> handleSearchPath(parsedUri)
            DeepLinks.PATH_DETAIL -> handleDetailPath(parsedUri)
            else -> throw IllegalArgumentException("Unknown Deeplink: $parsedUri")
        }

    private fun handleSearchPath(parsedUri: Uri): NavDirections {
        val pathQuery = parsedUri.getQueryParameter(DeepLinks.QUERY_SEARCH_TERM)
        val searchQuery =
            requireNotNull(pathQuery) { "search term is required." }
        return DealsFragmentDirections.actionDealsFragmentToSearchFragment(searchQuery)
    }

    private fun handleDetailPath(parsedUri: Uri): NavDirections {
        val queryTitle = parsedUri.getQueryParameter(DeepLinks.QUERY_TITLE)
        val title =
            requireNotNull(queryTitle) { "title is required to open details" }

        val queryPlainId = parsedUri.getQueryParameter(DeepLinks.QUERY_PLAIN_ID)
        val plainId =
            requireNotNull(queryPlainId) { "plainId is required to open details" }

        val queryBuyUrl = parsedUri.getQueryParameter(DeepLinks.QUERY_BUY_URL)
        val buyUrl =
            requireNotNull(queryBuyUrl) { "buyUrl is required to open details." }

        return DetailsFragmentDirections.actionGlobalGameDetailFragment(plainId, title, buyUrl)
    }
}
