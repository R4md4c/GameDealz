package de.r4md4c.gamedealz.common.navigator

import android.net.Uri
import androidx.navigation.NavController
import de.r4md4c.gamedealz.common.deepllink.DeepLinks
import de.r4md4c.gamedealz.deals.DealsFragmentDirections

class AndroidNavigator(private val navController: NavController) : Navigator {

    override fun navigate(uri: String) {
        val parsedUri = Uri.parse(uri)
        val searchQuery = parsedUri.getQueryParameter(DeepLinks.QUERY_SEARCH_TERM)
            ?: throw IllegalArgumentException("Expect search_term to open search fragment.")
        navController.navigate(DealsFragmentDirections.actionDealsFragmentToSearchFragment(searchQuery))
    }

    override fun navigateUp() = navController.navigateUp()

}