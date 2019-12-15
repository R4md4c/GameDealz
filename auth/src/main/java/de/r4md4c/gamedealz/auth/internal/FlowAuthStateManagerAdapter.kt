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

import de.r4md4c.gamedealz.auth.AuthStateFlow
import de.r4md4c.gamedealz.auth.state.AuthorizationState
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.TokenResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class adapts the [InternalAuthStateManager] to be reactive by exposing a Flow.
 */
@Singleton
internal class FlowAuthStateManagerAdapter @Inject constructor(
    private val authStateManager: InternalAuthStateManager
) : AuthStateManager, AuthStateFlow {

    private val channel = ConflatedBroadcastChannel(authStateManager.currentAuthState)

    override val authorizationState: Flow<AuthorizationState> =
        channel.asFlow()
            .map { it.toAuthorizationState() }

    override fun updateAuthStateAfterAuthorization(
        authorizationResponse: AuthorizationResponse?,
        exception: AuthorizationException?
    ) {
        authStateManager.updateAuthStateAfterAuthorization(authorizationResponse, exception)
        channel.sendBlocking(authStateManager.currentAuthState)
    }

    override fun updateAuthStateAfterToken(
        tokenResponse: TokenResponse?,
        exception: AuthorizationException?
    ) {
        authStateManager.updateAuthStateAfterToken(tokenResponse, exception)
        channel.sendBlocking(authStateManager.currentAuthState)
    }

    private fun AuthState.toAuthorizationState(): AuthorizationState =
        when {
            isAuthorized -> AuthorizationState.TokenGranted(this.accessToken!!)
            lastAuthorizationResponse != null -> AuthorizationState.AuthorizationGranted
            authorizationException != null ->
                AuthorizationState.AuthorizationFailed(
                    authorizationException!!.errorDescription,
                    authorizationException!!.cause
                )
            else -> AuthorizationState.NotAuthorized
        }
}
