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

package de.r4md4c.gamedealz.common.deepllink

import android.net.Uri

object DeepLinks {
    private const val APP_SCHEME = "gamedealz"

    const val PATH_SEARCH = "search"
    const val PATH_DETAIL = "detail"
    private const val PATH_MANAGE_WATCHLIST = "manage_watch_list"

    const val QUERY_SEARCH_TERM = "search_term"
    const val QUERY_TITLE = "title"
    const val QUERY_PLAIN_ID = "plain_id"
    const val QUERY_BUY_URL = "buy_url"

    fun manageWatchlistDeepLink(): Uri =
        appSchemeUri.buildUpon()
            .appendPath(PATH_MANAGE_WATCHLIST)
            .build()

    fun buildSearchUri(searchTerm: String): Uri = appSchemeUri.buildUpon()
        .appendPath(PATH_SEARCH)
        .appendQueryParameter(QUERY_SEARCH_TERM, searchTerm)
        .build()

    fun buildDetailUri(plainId: String, title: String, buyUrl: String): Uri = appSchemeUri.buildUpon()
        .appendPath(PATH_DETAIL)
        .appendQueryParameter(QUERY_TITLE, title)
        .appendQueryParameter(QUERY_PLAIN_ID, plainId)
        .appendQueryParameter(QUERY_BUY_URL, buyUrl)
        .build()

    private val appSchemeUri = Uri.Builder()
        .scheme(APP_SCHEME)
        .build()
}
