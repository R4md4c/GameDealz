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

package de.r4md4c.gamedealz.feature.home.mvi.intent.helper

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.common.mvi.uiSideEffect
import de.r4md4c.gamedealz.domain.model.UserInfo
import de.r4md4c.gamedealz.domain.usecase.GetUserUseCase
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.feature.home.state.HomeUiSideEffect
import de.r4md4c.gamedealz.feature.home.state.HomeUserStatus
import de.r4md4c.gamedealz.test.mvi.FakeModelStore
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class UsersInitIntentHelperTest {

    @Mock
    lateinit var getUserUseCase: GetUserUseCase

    private val store = FakeModelStore(HomeMviViewState())

    @InjectMocks
    lateinit var subject: UsersInitIntentHelper

    @Test
    fun `should emit LoggedOut when user is logged out`() = runBlockingTest {
        ArrangeBuilder().withSingleUserInfo(UserInfo.UserLoggedOut)

        with(subject) { observeUser(store) }

        val result = store.lastValue()
        assertThat(result).isEqualTo(HomeMviViewState().copy(homeUserStatus = HomeUserStatus.LoggedOut))
    }

    @Test
    fun `should emit LoggedOut with authentication error side effect when user login attempts fails`() =
        runBlockingTest {
            ArrangeBuilder().withSingleUserInfo(UserInfo.LoggingUserFailed(reason = "aMessage"))

            with(subject) { observeUser(store) }

            val result = store.lastValue()
            assertThat(result).isEqualTo(HomeMviViewState().copy(
                homeUserStatus = HomeUserStatus.LoggedOut,
                uiSideEffect = uiSideEffect { HomeUiSideEffect.ShowAuthenticationError(message = "aMessage") }
            ))
        }

    @Test
    fun `should emit KnownUser without uiSideEffects when use case emits LoggedInUser`() =
        runBlockingTest {
            ArrangeBuilder().withSingleUserInfo(UserInfo.LoggedInUser(username = "aUser"))

            with(subject) { observeUser(store) }

            val result = store.lastValue()
            assertThat(result).isEqualTo(
                HomeMviViewState().copy(
                    homeUserStatus = HomeUserStatus.LoggedIn.KnownUser(username = "aUser")
                )
            )
        }

    @Test
    fun `should emit KnownUser with uiSideEffect when user logs in after being logged out`() =
        runBlockingTest {
            ArrangeBuilder().withMultipleUsers(
                listOf(
                    UserInfo.UserLoggedOut,
                    UserInfo.LoggedInUser(username = "aUser")
                )
            )

            with(subject) { observeUser(store) }

            val result = store.lastValue()
            assertThat(result).isEqualTo(HomeMviViewState().copy(
                homeUserStatus = HomeUserStatus.LoggedIn.KnownUser(username = "aUser"),
                uiSideEffect = uiSideEffect { HomeUiSideEffect.NotifyUserHasLoggedIn("aUser") }
            ))
        }

    @Test
    fun `should emit UnknownUser with uiSideEffect when user logs in as Unknown after being logged out`() =
        runBlockingTest {
            ArrangeBuilder().withMultipleUsers(
                listOf(
                    UserInfo.UserLoggedOut,
                    UserInfo.LoggedInUnknownUser
                )
            )

            with(subject) { observeUser(store) }

            val result = store.lastValue()
            assertThat(result).isEqualTo(HomeMviViewState().copy(
                homeUserStatus = HomeUserStatus.LoggedIn.UnknownUser,
                uiSideEffect = uiSideEffect { HomeUiSideEffect.NotifyUserHasLoggedIn(null) }
            ))
        }

    @Test
    fun `should emit KnownUser with no uiSideEffect when use case emits two LoggedInUser then LoggedInUser`() =
        runBlockingTest {
            ArrangeBuilder()
                .withMultipleUsers(listOf(UserInfo.LoggedInUser(username = "aUser")))

            with(subject) { observeUser(store) }

            val result = store.lastValue()
            assertThat(result).isEqualTo(
                HomeMviViewState().copy(
                    homeUserStatus = HomeUserStatus.LoggedIn.KnownUser(username = "aUser")
                )
            )
        }

    private inner class ArrangeBuilder {
        fun withSingleUserInfo(userInfo: UserInfo) = apply {
            runBlockingTest {
                whenever(getUserUseCase.invoke(anyOrNull())).thenReturn(flowOf(userInfo))
            }
        }

        fun withMultipleUsers(list: List<UserInfo>) = apply {
            runBlockingTest {
                whenever(getUserUseCase.invoke(anyOrNull())).thenReturn(list.asFlow())
            }
        }
    }
}
