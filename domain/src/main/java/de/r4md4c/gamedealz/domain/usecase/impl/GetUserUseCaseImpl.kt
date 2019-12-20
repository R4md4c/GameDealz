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

package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.gamedealz.auth.AuthStateFlow
import de.r4md4c.gamedealz.auth.state.AuthorizationState
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.UserInfo
import de.r4md4c.gamedealz.domain.usecase.GetUserUseCase
import de.r4md4c.gamedealz.network.model.AccessToken
import de.r4md4c.gamedealz.network.repository.UserRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class GetUserUseCaseImpl @Inject constructor(
    private val authState: AuthStateFlow,
    private val sharedPreferencesProvider: SharedPreferencesProvider,
    private val userRemoteRepository: UserRemoteRepository
) : GetUserUseCase {

    override suspend fun invoke(param: VoidParameter?): Flow<UserInfo> =
        authState.authorizationState.flatMapLatest { authState ->
            when (authState) {
                is AuthorizationState.TokenGranted -> retrieveUserWhenGranted(authState)
                is AuthorizationState.NotAuthorized, AuthorizationState.AuthorizationGranted ->
                    flowOf(UserInfo.UserLoggedOut)
                is AuthorizationState.AuthorizationFailed -> flowOf(
                    UserInfo.LoggingUserFailed(
                        authState.message
                    )
                )
            }
        }

    private fun retrieveUserWhenGranted(authState: AuthorizationState.TokenGranted): Flow<UserInfo.LoggedInUser> {
        return flow {
            val localUser = sharedPreferencesProvider.userName
            if (localUser.isEmpty()) {
                val remoteUser = withContext(Dispatchers.IO) {
                    userRemoteRepository.user(AccessToken(authState.accessToken))
                }
                sharedPreferencesProvider.userName = remoteUser.username
                emit(UserInfo.LoggedInUser(remoteUser.username))
            } else {
                emit(UserInfo.LoggedInUser(localUser))
            }
        }
    }
}
