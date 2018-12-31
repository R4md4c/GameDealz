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

package de.r4md4c.gamedealz.network.service

import de.r4md4c.gamedealz.network.model.Plain
import de.r4md4c.gamedealz.network.model.SearchResult
import de.r4md4c.gamedealz.network.scrapper.Scrapper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

internal class IsThereAnyDealScrappingService(private val scrapper: Scrapper) : SearchService {

    override suspend fun search(searchTerm: String): List<SearchResult> = withContext(IO) {
        require(searchTerm.isNotEmpty()) { "searchTerm should not be empty" }

        val document = scrapper.scrap(SEARCH_URL.format(searchTerm))

        document.select(".card-container")
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