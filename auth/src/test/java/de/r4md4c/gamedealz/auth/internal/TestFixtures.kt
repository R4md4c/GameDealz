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

import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.TokenResponse
import java.nio.charset.Charset

object TestFixtures {

    val authorizationResponse: AuthorizationResponse
        get() {
            val authJson = readStringFromResources("json/authorization_response.json")
            return AuthorizationResponse.jsonDeserialize(authJson)
        }

    val authorizationException: AuthorizationException
        get() = AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW

    val tokenResponse: TokenResponse
        get() {
            val tokenJson = readStringFromResources("json/token_response.json")
            return TokenResponse.jsonDeserialize(tokenJson)
        }

    val authState: AuthState
        get() {
            val authJson = readStringFromResources("json/serialized_auth_state.json")
            return AuthState.jsonDeserialize(authJson)
        }

    private fun readStringFromResources(resourcesPath: String) =
        javaClass.classLoader!!.getResourceAsStream(resourcesPath)
            .readBytes().toString(Charset.defaultCharset())
}
