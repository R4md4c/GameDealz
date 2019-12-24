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

package de.r4md4c.gamedealz.auth.internal

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import de.r4md4c.gamedealz.auth.AuthActivityDelegate
import de.r4md4c.gamedealz.common.IDispatchers
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import timber.log.Timber
import javax.inject.Inject

private const val AUTH_REQUEST_CODE = 0x10

internal class AppAuthActivityDelegate @Inject constructor(
    private val authRequestBuilder: AuthorizationRequest.Builder,
    private val authorizationService: AuthorizationService,
    private val accessTokenRetriever: AccessTokenRetriever,
    private val dispatchers: IDispatchers,
    private val authStateManager: AuthStateManager
) : AuthActivityDelegate {

    override fun onActivityResult(activity: FragmentActivity, requestCode: Int, data: Intent?) {
        if (requestCode != AUTH_REQUEST_CODE || data == null) {
            return
        }

        val resp = AuthorizationResponse.fromIntent(data)
        val ex = AuthorizationException.fromIntent(data)

        activity.lifecycleScope.launchWhenResumed {
            withContext(dispatchers.Default) {
                updateAfterAuthorization(resp, ex)
            }

            // Only retrieve the token if there was no exception while getting authorized.
            withContext(dispatchers.IO) {
                if (ex == null) {
                    updateAfterToken(resp)
                }
            }
        }
    }

    override fun startAuthFlow(activity: FragmentActivity) {
        val intent = authorizationService.getAuthorizationRequestIntent(authRequestBuilder.build())
        activity.startActivityForResult(intent, AUTH_REQUEST_CODE)
    }

    private fun updateAfterAuthorization(
        resp: AuthorizationResponse?,
        ex: AuthorizationException?
    ) = authStateManager.updateAuthStateAfterAuthorization(resp, ex)

    private suspend fun updateAfterToken(resp: AuthorizationResponse?) {
        kotlin.runCatching { accessTokenRetriever.retrieveAccessToken(resp!!) }
            .onSuccess {
                authStateManager.updateAuthStateAfterToken(it, null)
            }.onFailure {
                Timber.e(it, "Failed to retrieveAccessToken")
                authStateManager.updateAuthStateAfterToken(null, it as AuthorizationException)
            }
    }
}
