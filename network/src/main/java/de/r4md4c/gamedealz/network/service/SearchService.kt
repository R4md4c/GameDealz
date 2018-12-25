package de.r4md4c.gamedealz.network.service

import de.r4md4c.gamedealz.network.model.SearchResult

interface SearchService {

    /**
     * Scraps https://isthereanydeal.com/search/?q=search_term for plain ids.
     *
     * @param searchTerm the search term
     * @return a List of plain ids.
     */
    suspend fun search(searchTerm: String): List<SearchResult>
}