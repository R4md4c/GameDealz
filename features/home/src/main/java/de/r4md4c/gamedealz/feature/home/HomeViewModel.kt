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

package de.r4md4c.gamedealz.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.r4md4c.commonproviders.appcompat.NightMode
import de.r4md4c.gamedealz.common.message.UIMessage
import de.r4md4c.gamedealz.common.message.UIMessageManager
import de.r4md4c.gamedealz.common.runSuspendCatching
import de.r4md4c.gamedealz.domain.model.UserInfo
import de.r4md4c.gamedealz.domain.usecase.GetAlertsCountUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.GetUserUseCase
import de.r4md4c.gamedealz.domain.usecase.LogoutUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.domain.usecase.OnNightModeChangeUseCase
import de.r4md4c.gamedealz.domain.usecase.ToggleNightModeUseCase
import de.r4md4c.gamedealz.feature.home.state.HomeUserStatus
import de.r4md4c.gamedealz.feature.home.state.HomeViewState
import de.r4md4c.gamedealz.feature.home.state.PriceAlertCount
import de.r4md4c.gamedealz.feature.home.state.RegionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

internal class HomeViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val reactiveRegionUseCase: OnCurrentActiveRegionReactiveUseCase,
    private val getStoresUseCase: GetStoresUseCase,
    private val priceAlertsCountUseCase: GetAlertsCountUseCase,
    private val nightModeChangeUseCase: OnNightModeChangeUseCase,
    private val toggleNightModeUseCase: ToggleNightModeUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val uiMessageManager = UIMessageManager<HomeUIMessage>()
    private val regionStatus = MutableStateFlow<RegionStatus>(RegionStatus.Loading)
    private val homeUserStatus = MutableStateFlow<HomeUserStatus>(HomeUserStatus.LoggedOut)
    private val nighModeEnabled = MutableStateFlow(false)
    private val priceAlertCount = MutableStateFlow<PriceAlertCount>(PriceAlertCount.NotSet)
    private val userInfoFlow = getUserUseCase.invoke()
        .shareIn(viewModelScope, SharingStarted.Lazily)

    val state by lazy {
        combine(
            regionStatus,
            userInfoFlow,
            nighModeEnabled,
            priceAlertCount,
            uiMessageManager.messages,
        ) { regionStatus: RegionStatus, userInfo: UserInfo, nighModeEnabled: Boolean, priceAlertCount: PriceAlertCount, message ->
            HomeViewState(
                regionStatus = regionStatus,
                homeUserStatus = HomeUserStatus.fromUserInfo(userInfo),
                nightModeEnabled = nighModeEnabled,
                priceAlertsCount = priceAlertCount,
                uiMessage = message
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeViewState())
            .also {
                init()
            }
    }

    fun clearMessage(uiMessage: HomeUIMessage) {
        viewModelScope.launch {
            uiMessageManager.clearMessage(uiMessage.id)
        }
    }

    fun onToggleNightMode() {
        toggleNightModeUseCase.invoke()
    }

    fun onLogout() {
        viewModelScope.launch {
            logoutUseCase.invoke()
        }
    }

    private fun init() {
        observeUserInfo()
        observeActiveRegion()
        observePriceAlertsCount()
        observeNightModeChange()
    }

    private fun observeNightModeChange() {
        nightModeChangeUseCase.activeNightModeChange()
            .onEach { nightMode ->
                nighModeEnabled.value = nightMode == NightMode.Enabled
            }.launchIn(viewModelScope)
    }

    private fun observePriceAlertsCount() {
        priceAlertsCountUseCase.invoke()
            .map { count -> if (count == 0) PriceAlertCount.NotSet else PriceAlertCount.Set(count) }
            .onEach { alertCount ->
                priceAlertCount.value = alertCount
            }.launchIn(viewModelScope)
    }

    private fun observeActiveRegion() {
        flow {
            runSuspendCatching { activeRegionUseCase.invoke() }
                .map(RegionStatus::Active)
                .onSuccess { emit(it) }
                .onFailure {
                    Timber.e(it, "Failure to retrieve active region")
                }
        }.onStart<RegionStatus> { emit(RegionStatus.Loading) }
            .onEach { status ->
                regionStatus.value = status
            }
            .launchIn(viewModelScope)

        reactiveRegionUseCase.activeRegionChange()
            .onEach { activeRegion ->
                regionStatus.value = RegionStatus.Active(activeRegion)
                getStoresUseCase.invoke(activeRegion).first()
            }
            .launchIn(viewModelScope)
    }

    private fun observeUserInfo() {
        userInfoFlow.onEach { userInfo ->
            when (userInfo) {
                UserInfo.LoggedInUnknownUser -> {
                    uiMessageManager.emitUIMessage(HomeUIMessage.NotifyUserHasLoggedIn(null))
                }

                is UserInfo.LoggedInUser -> {
                    uiMessageManager.emitUIMessage(HomeUIMessage.NotifyUserHasLoggedIn(userInfo.username))
                }

                UserInfo.UserLoggedOut -> {
                    uiMessageManager.emitUIMessage(HomeUIMessage.NotifyUserHasLoggedOut)
                }
                    is UserInfo.LoggingUserFailed -> uiMessageManager.emitUIMessage(
                        HomeUIMessage.ShowAuthenticationError(
                            userInfo.reason
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}

internal sealed class HomeUIMessage : UIMessage() {
    data class ShowAuthenticationError(val reason: String) : HomeUIMessage()
    data class NotifyUserHasLoggedIn(val username: String?) : HomeUIMessage()
    data object NotifyUserHasLoggedOut : HomeUIMessage()
}
