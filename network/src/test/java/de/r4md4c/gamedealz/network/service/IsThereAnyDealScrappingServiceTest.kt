package de.r4md4c.gamedealz.network.service

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.squareup.moshi.Moshi
import de.r4md4c.gamedealz.network.json.ApplicationJsonAdapterFactory
import de.r4md4c.gamedealz.network.model.HighlightsXMLHttpResponse
import de.r4md4c.gamedealz.network.model.Plain
import de.r4md4c.gamedealz.network.scrapper.Scrapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.source
import org.jsoup.Jsoup
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.File

class IsThereAnyDealScrappingServiceTest {

    @Mock
    private lateinit var scrapper: Scrapper

    @Mock
    private lateinit var isThereAnyDealService: IsThereAnyDealService

    private lateinit var subject: IsThereAnyDealScrappingService

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        subject = IsThereAnyDealScrappingService(scrapper, isThereAnyDealService)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `search when searchTerm is empty`() {
        runBlocking {
            subject.search("")
        }
    }

    @Test
    fun `search returns plains when results are found`() {
        runBlocking {
            whenever(scrapper.scrap(any())).thenReturn(Jsoup.parse(readSearchItems()))

            val results = subject.search("searchTerm")

            verify(scrapper).scrap(SEARCH_URL.format("searchTerm"))
            assertThat(results.map { it.plain.value }).isEqualTo(
                listOf(
                    "battlefieldv", "battlefieldvdeluxeedition", "battlefieldvstandardedition", "battlefieldvopenbeta",
                    "battlefieldvtrial", "battlefieldvdeluxeeditionupgrade", "battlefieldvidiomaapenasemingles"
                )
            )
            assertThat(results.map { it.title }).isEqualTo(
                listOf(
                    "Battlefield™ V", "Battlefield™ V Deluxe Edition",
                    "Battlefield™ V Standard Edition", "Battlefield™ V Open Beta", "Battlefield™ V (Trial)",
                    "Battlefield™ V Deluxe Edition Upgrade", "Battlefield V - Idioma Apenas em Inglês"
                )
            )
        }
    }

    @Test
    fun `search returns empty list when results are not found`() {
        runBlocking {
            whenever(scrapper.scrap(any())).thenReturn(Jsoup.parse(readEmptySearchItems()))

            val results = subject.search("searchTerm")

            verify(scrapper).scrap(SEARCH_URL.format("searchTerm"))
            assertThat(results).isEqualTo(emptyList<Plain>())
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `highlights when regionCode is empty`() {
        runBlocking {
            subject.highlights("", "")
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `highlights when countryCode is empty`() {
        runBlocking {
            subject.highlights("regionCode", "")
        }
    }

    @Test
    fun `highlights returns correct plain ids from response`() {
        runBlocking {
            ArrangeBuilder()
                .withIsThereAnyDealServiceResponse()
                .withRealScrapper()

            val plains = subject.highlights("regionCode", "countryCode")

            verify(isThereAnyDealService).highlights(eq("region=regionCode;country=countryCode"), any(), any())
            assertThat(plains).containsExactly(
                Plain("yakuza0"),
                Plain("tomclancysghostreconwildlands"),
                Plain("crusaderkingsii"),
                Plain("middleearthshadowofwar"),
                Plain("dragonquestxiechoesofanelusiveagedigitaleditionoflight"),
                Plain("donutcounty")
            )
        }
    }


    private fun readSearchItems(): String {
        val classLoader = javaClass.classLoader
        val file = File(classLoader!!.getResource("search_items.html").file)
        return file.source().buffer().readUtf8()
    }

    private fun readEmptySearchItems(): String {
        val classLoader = javaClass.classLoader
        val file = File(classLoader!!.getResource("search_items_not_found.html").file)
        return file.source().buffer().readUtf8()
    }

    private fun readHighlightsResult(): HighlightsXMLHttpResponse {
        val moshi = Moshi.Builder()
            .add(ApplicationJsonAdapterFactory.INSTANCE)
            .build()
        val classLoader = javaClass.classLoader
        val file = File(classLoader!!.getResource("highlights_response_success.json").file)
        return moshi.adapter(HighlightsXMLHttpResponse::class.java).fromJson(file.source().buffer())!!
    }

    inner class ArrangeBuilder {

        fun withRealScrapper() = apply {
            runBlocking {
                doAnswer {
                    Jsoup.parseBodyFragment(it.getArgument(0))
                }.whenever(scrapper)
                    .scapFragment(any())
            }
        }

        fun withIsThereAnyDealServiceResponse() = apply {
            whenever(isThereAnyDealService.highlights(any(), any(), any())).thenReturn(
                CompletableDeferred(
                    readHighlightsResult()
                )
            )
        }
    }
}

private const val SEARCH_URL = "https://isthereanydeal.com/search/?q=%s"
