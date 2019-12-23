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

import de.r4md4c.gamedealz.auth.AccessTokenGetter
import de.r4md4c.gamedealz.network.model.AccessToken
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class InternalAccessTokenGetter @Inject constructor(
    private val authorizationService: AuthorizationService,
    private val clientAuthentication: ClientAuthentication,
    private val internalAuthStateManager: InternalAuthStateManager
) : AccessTokenGetter {

    override suspend fun ensureFreshAccessToken(): AccessToken = suspendCoroutine {
        internalAuthStateManager.currentAuthState
            .performActionWithFreshTokens(
                authorizationService,
                clientAuthentication
            ) { token, _, exception ->
                if (token != null) {
                    it.resume(AccessToken(token))
                } else {
                    it.resumeWithException(IllegalStateException(exception))
                }
            }
    }
}
