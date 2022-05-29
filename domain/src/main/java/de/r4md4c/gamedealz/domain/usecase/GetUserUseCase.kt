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

package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.gamedealz.auth.AccessTokenGetter
import de.r4md4c.gamedealz.auth.AuthStateFlow
import de.r4md4c.gamedealz.auth.state.AuthorizationState
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.domain.model.UserInfo
import de.r4md4c.gamedealz.network.model.User
import de.r4md4c.gamedealz.network.repository.UserRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val authState: AuthStateFlow,
    private val sharedPreferencesProvider: SharedPreferencesProvider,
    private val userRemoteRepository: UserRemoteRepository,
    private val accessTokenGetter: AccessTokenGetter,
    private val dispatchers: IDispatchers
) {

    fun invoke(): Flow<UserInfo> =
        authState.authorizationState.flatMapLatest { authState ->
            when (authState) {
                is AuthorizationState.TokenGranted -> retrieveUserWhenGranted()
                is AuthorizationState.NotAuthorized, AuthorizationState.AuthorizationGranted ->
                    flowOf(UserInfo.UserLoggedOut)
                is AuthorizationState.AuthorizationFailed -> flowOf(
                    UserInfo.LoggingUserFailed(
                        authState.message
                    )
                )
            }
        }

    private fun retrieveUserWhenGranted(): Flow<UserInfo> {
        return flow {
            val localUser = sharedPreferencesProvider.userName
            if (localUser.isEmpty()) {
                val remoteUserResult = kotlin.runCatching {
                    withContext(dispatchers.IO) {
                        val accessToken = accessTokenGetter.ensureFreshAccessToken()
                        userRemoteRepository.user(accessToken)
                    }
                }

                val remoteUser = remoteUserResult.getOrElse {
                    Timber.w(it, "Failed to get username from server, emitting LoggedInUnknownUser")
                    emit(UserInfo.LoggedInUnknownUser)
                    return@flow
                }

                val user = when (remoteUser) {
                    is User.KnownUser -> UserInfo.LoggedInUser(remoteUser.username)
                    is User.UnknownUser -> UserInfo.LoggedInUnknownUser
                }

                if (user is UserInfo.LoggedInUser) {
                    sharedPreferencesProvider.userName = user.username
                }

                emit(user)
            } else {
                emit(UserInfo.LoggedInUser(localUser))
            }
        }
    }
}
