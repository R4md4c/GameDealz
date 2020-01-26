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

import de.r4md4c.gamedealz.common.mvi.IntentProcessor
import de.r4md4c.gamedealz.domain.model.UserInfo
import de.r4md4c.gamedealz.domain.usecase.GetUserUseCase
import de.r4md4c.gamedealz.feature.home.mvi.AnonymousUserResult
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviResult
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent
import de.r4md4c.gamedealz.feature.home.mvi.KnownUserResult
import de.r4md4c.gamedealz.feature.home.mvi.LoginFailedResult
import de.r4md4c.gamedealz.feature.home.mvi.UserLoggedOutResult
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import javax.inject.Inject

internal class UserInitIntentProcessor @Inject constructor(
    private val getUserUseCase: GetUserUseCase
) : IntentProcessor<HomeMviViewEvent, HomeMviViewState> {

    /**
     * Observes the current user status, and notifies only in case of changes of authentication status.
     * e.g.
     * shouldNotify = true => when old status changed to new status.
     * shouldNotify = false => when the old status is the same as a the new status.
     *
     * These prevent cases when user is logged in and rotates the screen, and we still notify
     * him that he's logged in.
     */
    override fun process(viewEvent: Flow<HomeMviViewEvent>): Flow<HomeMviResult> =
        viewEvent.filterIsInstance<HomeMviViewEvent.InitViewEvent>()
            .flatMapLatest {
                val userInfo = getUserUseCase().first()
                getUserUseCase().scan(
                    UserInfoHistory(
                        prev = null,
                        current = userInfo
                    )
                ) { acc, new ->
                    acc.copy(current = new, prev = acc.current)
                }.map {
                    UserInfoResult(
                        info = it.current,
                        shouldNotify = (
                                it.current is UserInfo.LoggedInUser ||
                                        it.current is UserInfo.LoggedInUnknownUser
                                ) &&
                                it.prev != null && it.current != it.prev
                    )
                }.distinctUntilChanged().map { result ->
                    when (result.info) {
                        is UserInfo.LoggedInUser -> KnownUserResult(
                            shouldNotify = result.shouldNotify,
                            userName = result.info.username
                        )
                        is UserInfo.LoggedInUnknownUser -> AnonymousUserResult(result.shouldNotify)
                        is UserInfo.UserLoggedOut -> UserLoggedOutResult
                        is UserInfo.LoggingUserFailed -> LoginFailedResult(result.info.reason)
                    }
                }
            }

    private data class UserInfoHistory(val prev: UserInfo? = null, val current: UserInfo)
    private data class UserInfoResult(val info: UserInfo, val shouldNotify: Boolean)
}
