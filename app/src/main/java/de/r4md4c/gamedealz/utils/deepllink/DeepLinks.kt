package de.r4md4c.gamedealz.utils.deepllink

import android.net.Uri

object DeepLinks {
    private const val APP_SCHEME = "gamedealz"

    const val PATH_SEARCH = "search"
    const val QUERY_SEARCH_TERM = "search_term"

    fun buildSearchUri(searchTerm: String): Uri = appSchemeUri.buildUpon()
        .appendPath(PATH_SEARCH)
        .appendQueryParameter(QUERY_SEARCH_TERM, searchTerm)
        .build()


    private val appSchemeUri = Uri.Builder()
        .scheme(APP_SCHEME)
        .build()
}
