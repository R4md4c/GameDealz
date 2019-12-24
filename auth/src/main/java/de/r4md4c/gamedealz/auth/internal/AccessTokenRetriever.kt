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

import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.TokenResponse
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

internal class AccessTokenRetriever @Inject constructor(
    private val authorizationService: AuthorizationService,
    private val clientAuthentication: ClientAuthentication
) {

    suspend fun retrieveAccessToken(authorizationResponse: AuthorizationResponse): TokenResponse =
        suspendCoroutine { continuation ->
            authorizationService.performTokenRequest(
                authorizationResponse.createTokenExchangeRequest(),
                clientAuthentication
            ) { response, ex ->
                response?.let { continuation.resumeWith(Result.success(it)) }
                ex?.let { continuation.resumeWith(Result.failure(it)) }
            }
        }
}
