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

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationServiceConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InternalAuthStateManagerTest {

    private lateinit var authStateManager: InternalAuthStateManager

    private val context
        get() = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun beforeEach() {
        authStateManager = InternalAuthStateManager(context, config)
    }

    @After
    fun afterEach() {
        context.deleteSharedPreferences(SHARED_PREFS_NAME)
    }

    @Test
    fun `should return state with auth configuration inside it when shared prefs is empty`() {
        context.deleteSharedPreferences(SHARED_PREFS_NAME)

        val state = authStateManager.currentAuthState

        assertThat(state.authorizationServiceConfiguration!!.authorizationEndpoint)
            .isEqualTo(Uri.parse("http://itad/oauth/authorize"))
        assertThat(state.authorizationServiceConfiguration!!.tokenEndpoint)
            .isEqualTo(Uri.parse("http://itad/oauth/token"))
    }

    @Test
    fun `should return correct state with authorization response inside it when shared prefs has auth response`() {
        putAuthorizationResponseInSharedPrefs()

        val state = authStateManager.currentAuthState

        assertThat(state.lastAuthorizationResponse).isNotNull()
        assertThat(state.accessToken).isNull()
    }

    @Test
    fun `should return correct state with token response inside it when shared prefs has token response`() {
        putTokenResponseInSharedPrefs()

        val state = authStateManager.currentAuthState

        assertThat(state.accessToken).isNotNull()
        assertThat(state.isAuthorized).isTrue()
    }

    @Test
    fun `should return correct state with token response and authorization inside it when shared prefs has full auth state response`() {
        putFullSerializedAuthStateInSharedPrefs()

        val state = authStateManager.currentAuthState

        assertThat(state.lastAuthorizationResponse).isNotNull()
        assertThat(state.accessToken).isNotNull()
        assertThat(state.isAuthorized).isTrue()
    }

    @Test
    fun `should update currentAuthState with authorization response when updateAuthStateAfterAuthorization is called`() {
        authStateManager.updateAuthStateAfterAuthorization(TestFixtures.authorizationResponse, null)

        val state = authStateManager.currentAuthState

        assertThat(state.lastAuthorizationResponse).isNotNull()
        assertThat(state.accessToken).isNull()
    }

    @Test
    fun `should update currentAuthState with token response when updateAuthStateAfterToken is called`() {
        authStateManager.updateAuthStateAfterToken(TestFixtures.tokenResponse, null)

        val state = authStateManager.currentAuthState

        assertThat(state.accessToken).isNotNull()
        assertThat(state.isAuthorized).isTrue()
    }

    private fun putAuthorizationResponseInSharedPrefs() {
        getSharedPrefs().edit()
            .putString(KEY_AUTH_STATE,
                AuthState().also {
                    it.update(TestFixtures.authorizationResponse, null)
                }.jsonSerializeString()
            )
            .commit()
    }

    private fun putTokenResponseInSharedPrefs() {
        getSharedPrefs().edit()
            .putString(KEY_AUTH_STATE,
                AuthState().also {
                    it.update(TestFixtures.tokenResponse, null)
                }.jsonSerializeString()
            )
            .commit()
    }

    private fun putFullSerializedAuthStateInSharedPrefs() {
        getSharedPrefs().edit()
            .putString(KEY_AUTH_STATE, TestFixtures.authState.jsonSerializeString())
            .commit()
    }



    private fun getSharedPrefs() =
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    private companion object {
        private const val KEY_AUTH_STATE = "auth_state_key"
        private const val SHARED_PREFS_NAME = "Auth.prefs"

        private val authEndpoint = Uri.parse("http://itad/oauth/authorize")
        private val tokenEndpoint = Uri.parse("http://itad/oauth/token")
        private val config = AuthorizationServiceConfiguration(authEndpoint, tokenEndpoint)
    }
}
