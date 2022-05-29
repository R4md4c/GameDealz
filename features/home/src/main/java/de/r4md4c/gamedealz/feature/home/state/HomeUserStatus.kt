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

package de.r4md4c.gamedealz.feature.home.state

import de.r4md4c.gamedealz.domain.model.UserInfo

internal sealed class HomeUserStatus {
    object LoggedOut : HomeUserStatus() {
        override fun toString(): String = "UserNotLoggedIn"
    }

    sealed class LoggedIn : HomeUserStatus() {
        data class KnownUser(val username: String) : LoggedIn()
        object UnknownUser : LoggedIn() {
            override fun toString(): String = "UnknownUser"
        }
    }

    companion object {
        fun fromUserInfo(userInfo: UserInfo): HomeUserStatus {
            return when (userInfo) {
                UserInfo.LoggedInUnknownUser -> LoggedIn.UnknownUser
                is UserInfo.LoggedInUser -> LoggedIn.KnownUser(userInfo.username)
                is UserInfo.LoggingUserFailed -> LoggedOut
                UserInfo.UserLoggedOut -> LoggedOut
            }
        }
    }
}
