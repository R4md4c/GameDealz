package de.r4md4c.gamedealz.common.deepllink

import android.net.Uri

object DeepLinks {
    private const val APP_SCHEME = "gamedealz"

    const val PATH_SEARCH = "search"
    const val PATH_DETAIL = "detail"

    const val QUERY_SEARCH_TERM = "search_term"
    const val QUERY_TITLE = "title"
    const val QUERY_PLAIN_ID = "plain_id"

    fun buildSearchUri(searchTerm: String): Uri = appSchemeUri.buildUpon()
        .appendPath(PATH_SEARCH)
        .appendQueryParameter(QUERY_SEARCH_TERM, searchTerm)
        .build()

    fun buildDetailUri(plainId: String, title: String): Uri = appSchemeUri.buildUpon()
        .appendPath(PATH_DETAIL)
        .appendQueryParameter(QUERY_TITLE, title)
        .appendQueryParameter(QUERY_PLAIN_ID, plainId)
        .build()

    private val appSchemeUri = Uri.Builder()
        .scheme(APP_SCHEME)
        .build()
}
