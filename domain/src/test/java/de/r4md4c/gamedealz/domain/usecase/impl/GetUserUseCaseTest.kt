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
import de.r4md4c.gamedealz.auth.AccessTokenGetter
import de.r4md4c.gamedealz.auth.AuthStateFlow
import de.r4md4c.gamedealz.auth.state.AuthorizationState
import de.r4md4c.gamedealz.domain.model.UserInfo
import de.r4md4c.gamedealz.domain.usecase.GetUserUseCase
import de.r4md4c.gamedealz.network.model.AccessToken
import de.r4md4c.gamedealz.network.model.User
import de.r4md4c.gamedealz.network.repository.UserRemoteRepository
import de.r4md4c.gamedealz.test.TestDispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class GetUserUseCaseTest {

    @Mock
    private lateinit var authState: AuthStateFlow

    @Mock
    private lateinit var sharedPreferencesProvider: SharedPreferencesProvider

    @Mock
    private lateinit var userRemoteRepository: UserRemoteRepository

    @Mock
    private lateinit var accessTokenGetter: AccessTokenGetter

    private lateinit var getUserUseCase: GetUserUseCase

    @Before
    fun beforeEach() {
        getUserUseCase = GetUserUseCase(
            authState,
            sharedPreferencesProvider,
            userRemoteRepository,
            accessTokenGetter,
            TestDispatchers
        )
    }

    @Test
    fun `emits LoggedOut when auth state is NotAuthorized`() = runBlockingTest {
        ArrangeBuilder().withAuthState(AuthorizationState.NotAuthorized)

        val flow = getUserUseCase.invoke()

        flow.collect { assertThat(it).isEqualTo(UserInfo.UserLoggedOut) }
    }

    @Test
    fun `emits LoggedOut when auth state is AuthorizationGranted`() = runBlockingTest {
        ArrangeBuilder().withAuthState(AuthorizationState.AuthorizationGranted)

        val flow = getUserUseCase.invoke()

        flow.collect { assertThat(it).isEqualTo(UserInfo.UserLoggedOut) }
    }

    @Test
    fun `emits LoggingUserFailed when auth state is AuthorizationFailed`() = runBlockingTest {
        ArrangeBuilder().withAuthState(
            AuthorizationState.AuthorizationFailed(
                message = "Message",
                cause = null
            )
        )

        val flow = getUserUseCase.invoke()

        flow.collect { assertThat(it).isEqualTo(UserInfo.LoggingUserFailed(reason = "Message")) }
    }

    @Test
    fun `emits LoggedInUser when auth state is TokenGranted and sharedPreference returns name`() =
        runBlockingTest {
            ArrangeBuilder().withAuthState(
                AuthorizationState.TokenGranted(
                    accessToken = "Token"
                )
            ).withSharedPreferenceResult("aUser")

            val flow = getUserUseCase.invoke()

            flow.collect { assertThat(it).isEqualTo(UserInfo.LoggedInUser(username = "aUser")) }
        }

    @Test
    fun `emits LoggedInUser when auth state is TokenGranted, sharedPreference return empty name and userRepository returns logged In user`() =
        runBlockingTest {
            ArrangeBuilder()
                .withAuthState(
                    AuthorizationState.TokenGranted(
                        accessToken = "Token"
                    )
                )
                .withSharedPreferenceResult()
                .withRemoteRepositoryResponse(User.KnownUser("aUser"))


            val flow = getUserUseCase.invoke()

            flow.collect { assertThat(it).isEqualTo(UserInfo.LoggedInUser(username = "aUser")) }
        }

    @Test
    fun `emits LoggedInUnknown when auth state is TokenGranted, sharedPreference return empty name and userRepository returns unknown user`() =
        runBlockingTest {
            ArrangeBuilder()
                .withAuthState(
                    AuthorizationState.TokenGranted(
                        accessToken = "Token"
                    )
                )
                .withSharedPreferenceResult()
                .withRemoteRepositoryResponse(User.UnknownUser)


            val flow = getUserUseCase.invoke()

            flow.collect { assertThat(it).isEqualTo(UserInfo.LoggedInUnknownUser) }
        }

    @Test
    fun `sharedPreferences stores username when auth state is TokenGranted, sharedPreference return empty name and userRepository returns logged In user`() =
        runBlockingTest {
            ArrangeBuilder()
                .withAuthState(
                    AuthorizationState.TokenGranted(
                        accessToken = "Token"
                    )
                )
                .withSharedPreferenceResult()
                .withRemoteRepositoryResponse(User.KnownUser("aUser"))


            getUserUseCase.invoke().collect()

            verify(sharedPreferencesProvider).userName = "aUser"
        }

    @Test
    fun `emits LoggedInUnknownUser when repository fails to fetch username`() =
        runBlockingTest {
            ArrangeBuilder()
                .withAuthState(
                    AuthorizationState.TokenGranted(
                        accessToken = "Token"
                    )
                )
                .withSharedPreferenceResult()
                .withRemoteRepositoryError()


            val flow = getUserUseCase.invoke()

            flow.collect { assertThat(it).isEqualTo(UserInfo.LoggedInUnknownUser) }
        }

    inner class ArrangeBuilder {
        init {
            runBlockingTest {
                whenever(accessTokenGetter.ensureFreshAccessToken()).thenReturn(
                    AccessToken("Token")
                )
            }
        }

        fun withAuthState(state: AuthorizationState) = apply {
            whenever(authState.authorizationState).thenReturn(flowOf(state))
        }

        fun withRemoteRepositoryResponse(user: User) = apply {
            runBlocking { whenever(userRemoteRepository.user(any())).thenReturn(user) }
        }

        fun withSharedPreferenceResult(username: String = "") = apply {
            whenever(sharedPreferencesProvider.userName).thenReturn(username)
        }

        fun withRemoteRepositoryError() = apply {
            runBlocking { whenever(userRemoteRepository.user(any())).thenThrow(RuntimeException()) }
        }
    }
}
