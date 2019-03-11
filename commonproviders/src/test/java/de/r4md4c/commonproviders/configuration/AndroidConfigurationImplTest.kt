package de.r4md4c.commonproviders.configuration

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
class AndroidConfigurationImplTest {

    private lateinit var subject: AndroidConfigurationImpl

    @Before
    fun beforeEach() {
        subject = AndroidConfigurationImpl(RuntimeEnvironment.systemContext)
    }

    @Test
    @Config(qualifiers = "en-rUS")
    fun `getLocale when locale is us`() {
        assertThat(subject.locale).isEqualTo(Locale.US)
    }

    @Test
    @Config(qualifiers = "en-rGB")
    fun `getLocale when locale is uk`() {
        val result = subject.locale

        assertThat(result).isEqualTo(Locale.UK)
        print(result.displayCountry)
    }

    @Test
    @Config(qualifiers = "en")
    fun `getLocale when locale is just English`() {
        val result = subject.locale

        assertThat(result).isEqualTo(Locale.ENGLISH)
    }

    @Test
    @Config(qualifiers = "ar")
    fun `getLocale when locale is Arabic`() {
        val result = subject.locale

        assertThat(result).isEqualTo(Locale("ar"))
    }

}