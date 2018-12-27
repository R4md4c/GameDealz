package de.r4md4c.gamedealz.common.navigator

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import de.r4md4c.gamedealz.common.deepllink.DeepLinks
import de.r4md4c.gamedealz.deals.DealsFragmentDirections
import de.r4md4c.gamedealz.detail.DetailsFragmentDirections
import timber.log.Timber

class AndroidNavigator(
    private val context: Context,
    private val navController: NavController
) : Navigator {

    override fun navigate(uri: String) {
        val navDirection: NavDirections? = getNavDirections(Uri.parse(uri))
        navDirection?.let { navController.navigate(navDirection) }
    }

    override fun navigateUp() = navController.navigateUp()

    override fun navigateToUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun getNavDirections(parsedUri: Uri): NavDirections? =
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
                Timber.e("Unknown Deeplink: $parsedUri")
                null
            }
        }

}