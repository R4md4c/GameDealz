package de.r4md4c.gamedealz.network.service

import de.r4md4c.gamedealz.network.model.Plain
import de.r4md4c.gamedealz.network.model.SearchResult
import de.r4md4c.gamedealz.network.scrapper.Scrapper

internal class IsThereAnyDealScrappingService(private val scrapper: Scrapper) : SearchService {

    override suspend fun search(searchTerm: String): List<SearchResult> {
        require(searchTerm.isNotEmpty()) { "searchTerm should not be empty" }

        val document = scrapper.scrap(SEARCH_URL.format(searchTerm))

        return document.select(".card-container")
            .mapNotNull { element ->
                (element.selectFirst("a.card__img").attr("href").extractPlainId() ?: "") to
                        element.selectFirst("a.card__title").text()
            }
            .filter { it.first.isNotEmpty() && it.second.isNotEmpty() }
            .map { SearchResult(it.second, Plain(it.first)) }
    }

    private fun String.extractPlainId(): String? = this.split('/')[2]
}

private const val SEARCH_URL = "https://isthereanydeal.com/search/?q=%s"