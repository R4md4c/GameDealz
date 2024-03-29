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

package de.r4md4c.gamedealz.auth.internal

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import de.r4md4c.gamedealz.test.CoroutinesTestRule
import de.r4md4c.gamedealz.test.TestDispatchers
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@RunWith(RobolectricTestRunner::class)
class AppAuthActivityDelegateTest {

    private val authRequestBuilder = AuthorizationRequest.Builder(
        config, "clientId", ResponseTypeValues.CODE, Uri.parse("http://google.com")
    )

    @Mock
    internal lateinit var authorizationService: AuthorizationService

    @Mock
    internal lateinit var accessTokenRetriever: AccessTokenRetriever

    @Mock
    internal lateinit var authStateManager: AuthStateManager

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private lateinit var delegate: AppAuthActivityDelegate

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        delegate = AppAuthActivityDelegate(
            authRequestBuilder,
            authorizationService,
            accessTokenRetriever,
            TestDispatchers,
            authStateManager
        )
    }

    @Test
    fun `startAuthFlow should start activity with correct request code`() {
        whenever(authorizationService.getAuthorizationRequestIntent(any()))
            .thenReturn(Intent(Intent.ACTION_VIEW))
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()

        delegate.startAuthFlow(activity)

        val application = shadowOf(ApplicationProvider.getApplicationContext<Application>())
        assertThat(application.nextStartedActivity).isNotNull()
    }

    @Test
    fun `onActivityResult should return with no interactions when request code is not correct`() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()

        delegate.onActivityResult(activity, Activity.RESULT_OK, 0x11, null)

        verifyNoInteractions(authStateManager)
        verifyNoInteractions(accessTokenRetriever)
    }

    @Test
    fun `onActivityResult should return with no interactions when intent is null`() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()

        delegate.onActivityResult(activity, Activity.RESULT_OK, 0x10, null)

        verifyNoInteractions(authStateManager)
        verifyNoInteractions(accessTokenRetriever)
    }

    @Test
    fun `onActivityResult should call authStateManager when Intent has AuthorizationResponse`() =
        runTest {
            whenever(accessTokenRetriever.retrieveAccessToken(any())).thenReturn(TestFixtures.tokenResponse)
            val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
            val authorizationResponseIntent = Intent().apply {
                putExtra(
                    AuthorizationResponse.EXTRA_RESPONSE,
                    TestFixtures.authorizationResponse.jsonSerializeString()
                )
            }

            delegate.onActivityResult(
                activity,
                Activity.RESULT_OK,
                0x10,
                authorizationResponseIntent
            )

            verify(authStateManager).updateAuthStateAfterToken(any(), isNull())
        }

    @Test
    fun `onActivityResult should call nothing when resultCode is cancelled`() = runTest {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        val authorizationResponseIntent = Intent().apply {
            putExtra(
                AuthorizationResponse.EXTRA_RESPONSE,
                TestFixtures.authorizationResponse.jsonSerializeString()
            )
        }

        delegate.onActivityResult(
            activity,
            Activity.RESULT_CANCELED,
            0x10,
            authorizationResponseIntent
        )

        verifyNoInteractions(authStateManager)
        verifyNoInteractions(accessTokenRetriever)
    }

    @Test
    fun `onActivityResult should not call authStateManager when Intent has AuthorizationException`() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        val authorizationExceptionIntent = Intent().apply {
            putExtra(
                AuthorizationException.EXTRA_EXCEPTION,
                TestFixtures.authorizationException.toJsonString()
            )
        }

        delegate.onActivityResult(activity, Activity.RESULT_OK, 0x10, authorizationExceptionIntent)

        verify(authStateManager).updateAuthStateAfterAuthorization(isNull(), any())
    }

    @Test
    fun `onActivityResult should retrieve token then updateAuthStateAfterToken when Intent has AuthorizationResponse`() =
        runBlockingTest {
            whenever(accessTokenRetriever.retrieveAccessToken(any())).thenReturn(TestFixtures.tokenResponse)
            val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
            val authorizationResponseIntent = Intent().apply {
                putExtra(
                    AuthorizationResponse.EXTRA_RESPONSE,
                    TestFixtures.authorizationResponse.jsonSerializeString()
                )
            }

            delegate.onActivityResult(
                activity,
                Activity.RESULT_OK,
                0x10,
                authorizationResponseIntent
            )

            verify(accessTokenRetriever).retrieveAccessToken(any())
            verify(authStateManager).updateAuthStateAfterToken(any(), isNull())
        }

    @Test
    fun `onActivityResult should retrieve token then updateAuthStateAfterToken with error when Intent has AuthorizationException`() =
        runBlockingTest {
            whenever(accessTokenRetriever.retrieveAccessToken(any()))
                .thenAnswer {
                    runBlockingTest {
                        suspendCoroutine { it.resumeWithException(TestFixtures.authorizationException) }
                    }
                }
            val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
            val authorizationResponseIntent = Intent().apply {
                putExtra(
                    AuthorizationResponse.EXTRA_RESPONSE,
                    TestFixtures.authorizationResponse.jsonSerializeString()
                )
            }

            delegate.onActivityResult(
                activity,
                Activity.RESULT_OK,
                0x10,
                authorizationResponseIntent
            )

            verify(accessTokenRetriever).retrieveAccessToken(any())
            verify(authStateManager).updateAuthStateAfterToken(isNull(), any())
        }

    companion object {
        private val authEndpoint = Uri.parse("http://itad/oauth/authorize")
        private val tokenEndpoint = Uri.parse("http://itad/oauth/token")
        private val config = AuthorizationServiceConfiguration(authEndpoint, tokenEndpoint)
    }
}
