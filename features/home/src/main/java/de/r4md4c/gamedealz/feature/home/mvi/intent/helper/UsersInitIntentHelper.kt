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

import de.r4md4c.gamedealz.common.coroutines.lifecycleLog
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.intent
import de.r4md4c.gamedealz.common.mvi.uiSideEffect
import de.r4md4c.gamedealz.domain.model.UserInfo
import de.r4md4c.gamedealz.domain.usecase.GetUserUseCase
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.feature.home.state.HomeUiSideEffect
import de.r4md4c.gamedealz.feature.home.state.HomeUserStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class UsersInitIntentHelper @Inject constructor(
    private val getUserUseCase: GetUserUseCase
) {

    /**
     * Observes the current user status, and notifies only in case of changes of authentication status.
     * e.g.
     * shouldNotify = true => when old status changed to new status.
     * shouldNotify = false => when the old status is the same as a the new status.
     *
     * These prevent cases when user is logged in and rotates the screen, and we still notify
     * him that he's logged in.
     */
    fun CoroutineScope.observeUser(store: ModelStore<HomeMviViewState>) = launch {
        val userInfo = getUserUseCase().first()
        getUserUseCase().scan(UserInfoHistory(prev = null, current = userInfo)) { acc, new ->
            acc.copy(current = new, prev = acc.current)
        }.map {
            UserInfoResult(
                info = it.current,
                shouldNotify = (it.current is UserInfo.LoggedInUser || it.current is UserInfo.LoggedInUnknownUser)
                        && it.prev != null && it.current != it.prev
            )
        }.distinctUntilChanged().map { result ->
            when (result.info) {
                is UserInfo.LoggedInUser -> intent<HomeMviViewState> {
                    copy(
                        homeUserStatus = HomeUserStatus.LoggedIn.KnownUser(result.info.username),
                        uiSideEffect = if (result.shouldNotify) uiSideEffect {
                            HomeUiSideEffect.NotifyUserHasLoggedIn(
                                result.info.username
                            )
                        } else null
                    )
                }
                is UserInfo.LoggedInUnknownUser -> intent {
                    copy(
                        homeUserStatus = HomeUserStatus.LoggedIn.UnknownUser,
                        uiSideEffect = if (result.shouldNotify) uiSideEffect {
                            HomeUiSideEffect.NotifyUserHasLoggedIn(
                                null
                            )
                        } else null
                    )
                }
                is UserInfo.UserLoggedOut -> intent {
                    copy(homeUserStatus = HomeUserStatus.LoggedOut)
                }
                is UserInfo.LoggingUserFailed -> intent {
                    copy(uiSideEffect = uiSideEffect {
                        HomeUiSideEffect.ShowAuthenticationError(
                            result.info.reason
                        )
                    })
                }
            }
        }.lifecycleLog("UserInfoResult").collect(store::process)
    }

    private data class UserInfoHistory(val prev: UserInfo? = null, val current: UserInfo)
    private data class UserInfoResult(val info: UserInfo, val shouldNotify: Boolean)
}
