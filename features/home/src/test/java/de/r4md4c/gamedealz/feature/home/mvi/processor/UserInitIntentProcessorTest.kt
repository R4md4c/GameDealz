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

package de.r4md4c.gamedealz.feature.home.mvi.processor

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.common.mvi.Intent
import de.r4md4c.gamedealz.domain.model.UserInfo
import de.r4md4c.gamedealz.domain.usecase.GetUserUseCase
import de.r4md4c.gamedealz.feature.home.mvi.AnonymousUserResult
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviResult
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent
import de.r4md4c.gamedealz.feature.home.mvi.KnownUserResult
import de.r4md4c.gamedealz.feature.home.mvi.LoginFailedResult
import de.r4md4c.gamedealz.feature.home.mvi.UserLoggedOutResult
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.test.FlowRecorder
import de.r4md4c.gamedealz.test.recordWith
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UserInitIntentProcessorTest {

    @Mock
    lateinit var getUserUseCase: GetUserUseCase

    @InjectMocks
    private lateinit var processor: UserInitIntentProcessor

    @Test
    fun `should emit LoggedOut when user is logged out`() = runBlockingTest {
        ArrangeBuilder().withSingleUserInfo(UserInfo.UserLoggedOut)

        val recorder = FlowRecorder<HomeMviResult>(this)
        processor.process(flowOf(HomeMviViewEvent.InitViewEvent)).recordWith(recorder)

        Assertions.assertThat(recorder.last())
            .isEqualTo(UserLoggedOutResult)
    }

    @Test
    fun `should emit LoggedOut with authentication error side effect when user login attempts fails`() =
        runBlockingTest {
            ArrangeBuilder().withSingleUserInfo(UserInfo.LoggingUserFailed(reason = "aMessage"))

            val result = processor.process(flowOf(HomeMviViewEvent.InitViewEvent)).first()

            Assertions.assertThat(result).isEqualTo(LoginFailedResult("aMessage"))
        }

    @Test
    fun `should emit KnownUser without uiSideEffects when use case emits LoggedInUser`() =
        runBlockingTest {
            ArrangeBuilder().withSingleUserInfo(UserInfo.LoggedInUser(username = "aUser"))

            val result = processor.process(flowOf(HomeMviViewEvent.InitViewEvent)).first()

            Assertions.assertThat(result)
                .isEqualTo(KnownUserResult(userName = "aUser", shouldNotify = false))
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

            val result = processor.process(flowOf(HomeMviViewEvent.InitViewEvent)).toCollection(
                mutableListOf()
            )

            Assertions.assertThat(result.last())
                .isEqualTo(KnownUserResult(userName = "aUser", shouldNotify = true))
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

            val recorder = FlowRecorder<HomeMviResult>(this)
            processor.process(flowOf(HomeMviViewEvent.InitViewEvent)).recordWith(recorder)

            Assertions.assertThat(recorder.toList())
                .isEqualTo(listOf(UserLoggedOutResult, AnonymousUserResult(true)))
        }

    @Test
    fun `should emit KnownUser with no uiSideEffect when use case emits two LoggedInUser then LoggedInUser`() =
        runBlockingTest {
            ArrangeBuilder()
                .withMultipleUsers(listOf(UserInfo.LoggedInUser(username = "aUser")))

            val result = processor.process(flowOf(HomeMviViewEvent.InitViewEvent)).toCollection(
                mutableListOf()
            )

            Assertions.assertThat(result.last())
                .isEqualTo(KnownUserResult(userName = "aUser", shouldNotify = false))
        }

    private fun Intent<HomeMviViewState>.reduce() = this.reduce(HomeMviViewState())

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
