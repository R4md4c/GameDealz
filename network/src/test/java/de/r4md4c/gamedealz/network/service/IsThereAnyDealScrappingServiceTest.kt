package de.r4md4c.gamedealz.network.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.network.model.Plain
import de.r4md4c.gamedealz.network.scrapper.Scrapper
import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.source
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.File

class IsThereAnyDealScrappingServiceTest {

    @Mock
    private lateinit var scapper: Scrapper

    private lateinit var subject: IsThereAnyDealScrappingService

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        subject = IsThereAnyDealScrappingService(scapper)
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
            whenever(scapper.scrap(any())).thenReturn(Jsoup.parse(readSearchItems()))

            val results = subject.search("searchTerm")

            verify(scapper).scrap(SEARCH_URL.format("searchTerm"))
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
            whenever(scapper.scrap(any())).thenReturn(Jsoup.parse(readEmptySearchItems()))

            val results = subject.search("searchTerm")

            verify(scapper).scrap(SEARCH_URL.format("searchTerm"))
            assertThat(results).isEqualTo(emptyList<Plain>())
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
}

private const val SEARCH_URL = "https://isthereanydeal.com/search/?q=%s"
