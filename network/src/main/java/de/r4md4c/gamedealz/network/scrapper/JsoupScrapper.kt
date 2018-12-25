package de.r4md4c.gamedealz.network.scrapper

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit

internal class JsoupScrapper(okHttpClient: OkHttpClient) : Scrapper {

    private val okHttpClient = okHttpClient.newBuilder()
        .callTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun scrap(url: String): Document {
        val responseString = withContext(IO) {
            okHttpClient.newCall(Request.Builder().url(url).get().build()).execute().body()?.string()
        }
        return Jsoup.parse(responseString)
    }

}