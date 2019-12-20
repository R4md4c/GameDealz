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

package de.r4md4c.gamedealz.auth.state

/**
 * Represents different of authorization state for a user.
 */
sealed class AuthorizationState {

    /**
     * The default state when the user hasn't yet logged in.
     */
    object NotAuthorized : AuthorizationState()

    /**
     * The user has authorized the client but hasn't yet requested a token.
     */
    object AuthorizationGranted : AuthorizationState()

    /**
     * The user has successfully requested a token and now he's ready to use the API with that token.
     */
    data class TokenGranted(val accessToken: String) : AuthorizationState()

    /**
     * The Authorization flow has failed
     */
    data class AuthorizationFailed(val message: String, val cause: Throwable?) :
        AuthorizationState()
}
