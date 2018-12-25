package de.r4md4c.gamedealz.network.scrapper

import org.jsoup.nodes.Document

internal interface Scrapper {

    /**
     * Scrap url and returns a Jsoup [Document].
     */
    suspend fun scrap(url: String): Document
}