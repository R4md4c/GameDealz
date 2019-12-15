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
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenResponse
import org.json.JSONException
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

@Singleton
internal class InternalAuthStateManager @Inject constructor(
    context: Context,
    private val authorizationServiceConfiguration: AuthorizationServiceConfiguration
) : AuthStateManager {

    private val authSharedPreference = context.getSharedPreferences(AUTH, Context.MODE_PRIVATE)
    private val sharedPrefsLock = ReentrantLock()
    private val authStateReference = AtomicReference<AuthState>()

    val currentAuthState: AuthState
        get() {
            if (authStateReference.get() != null) {
                return authStateReference.get()
            }

            val readState = readState()
            return if (authStateReference.compareAndSet(null, readState)) {
                readState
            } else {
                authStateReference.get()
            }
        }

    override fun updateAuthStateAfterAuthorization(
        authorizationResponse: AuthorizationResponse?,
        exception: AuthorizationException?
    ) {
        val state = this.currentAuthState
        state.update(authorizationResponse, exception)
        writeAuthState(state)
        authStateReference.set(state)
    }

    override fun updateAuthStateAfterToken(
        tokenResponse: TokenResponse?,
        exception: AuthorizationException?
    ) {
        val state = this.currentAuthState
        state.update(tokenResponse, exception)
        writeAuthState(state)
        authStateReference.set(state)
    }

    private fun readState(): AuthState = sharedPrefsLock.withLock {
        if (!authSharedPreference.contains(KEY_AUTH_STATE)) {
            return@withLock AuthState(authorizationServiceConfiguration)
        }

        val jsonString = authSharedPreference.getString(KEY_AUTH_STATE, null)!!
        return@withLock try {
            AuthState.jsonDeserialize(jsonString)
        } catch (ex: JSONException) {
            Timber.e(ex, "Failed to deserialize State")
            AuthState(authorizationServiceConfiguration)
        }
    }

    private fun writeAuthState(state: AuthState) = sharedPrefsLock.withLock {
        val serialized = state.jsonSerializeString()
        val hasCommitted = authSharedPreference.edit()
            .putString(KEY_AUTH_STATE, serialized)
            .commit()

        if (!hasCommitted) {
            throw IllegalStateException("Failed to write state to shared prefs")
        }
    }
}

private const val KEY_AUTH_STATE = "auth_state_key"
private const val AUTH = "Auth.prefs"
