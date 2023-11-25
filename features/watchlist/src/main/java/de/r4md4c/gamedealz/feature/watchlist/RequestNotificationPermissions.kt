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

package de.r4md4c.gamedealz.feature.watchlist

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class RequestNotificationPermissions @Inject constructor(
    private val fragment: Fragment,
) {

    @SuppressLint("InlinedApi")
    private val isPermissionGranted = MutableStateFlow<State>(
        State.Result(
            isGranted = ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    )

    private val launcher =
        fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            isPermissionGranted.value = State.Result(isGranted)
        }

    suspend fun requestNotificationPermission(): Boolean {
        val currentState = isPermissionGranted.value
        if (currentState is State.Result && currentState.isGranted) {
            return true
        }

        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            fragment.requireActivity(),
            Manifest.permission.POST_NOTIFICATIONS
        )

        if (shouldShowRationale && !showRationaleDialog()) {
            return false
        }

        isPermissionGranted.value = State.WaitingResult

        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)

        return isPermissionGranted.filterIsInstance<State.Result>().map { it.isGranted }.first()
    }

    private suspend fun showRationaleDialog() = suspendCoroutine { cont ->
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setMessage(fragment.getString(R.string.watchlist_please_allow_notification_permissions))
            .setPositiveButton(android.R.string.ok) { dialog, _ -> cont.resume(true); dialog.dismiss() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> cont.resume(false); dialog.dismiss(); }
            .show()
    }

    private sealed class State {
        data object WaitingResult : State()
        data class Result(val isGranted: Boolean) : State()
    }
}
