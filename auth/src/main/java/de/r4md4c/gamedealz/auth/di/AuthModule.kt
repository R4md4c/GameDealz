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

package de.r4md4c.gamedealz.auth.di

import android.content.Context
import android.net.Uri
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.r4md4c.gamedealz.network.BuildConfig
import de.r4md4c.gamedealz.network.di.URL_IS_THERE_ANY_DEAL
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.ResponseTypeValues
import okhttp3.OkHttpClient
import okhttp3.OkUrlFactory
import java.net.URL

@Module
object AuthModule {

    @Reusable
    @Provides
    fun authServiceConfiguration(): AuthorizationServiceConfiguration {
        val authUri = Uri.parse(URL_IS_THERE_ANY_DEAL).buildUpon()
            .appendPath("oauth")
            .appendPath("authorize")
            .build()
        val tokenUri = Uri.parse(URL_IS_THERE_ANY_DEAL).buildUpon()
            .appendPath("oauth")
            .appendPath("token")
            .build()
        return AuthorizationServiceConfiguration(authUri, tokenUri)
    }

    @Provides
    fun provideAuthRequestBuilder(config: AuthorizationServiceConfiguration): AuthorizationRequest.Builder =
        AuthorizationRequest.Builder(
            config,
            BuildConfig.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URI)
        ).apply { setScopes(SCOPES) }

    @Reusable
    @Provides
    fun provideAuthorizationService(
        context: Context,
        okHttpClient: OkHttpClient
    ): AuthorizationService {
        @Suppress("DEPRECATION") val urlOkFactory = OkUrlFactory(okHttpClient)

        return AuthorizationService(context,
            AppAuthConfiguration.Builder()
                .setConnectionBuilder { urlOkFactory.open(URL(it.toString())) }
                .build()
        )
    }

    @Reusable
    @Provides
    fun provideClientAuthentication(): ClientAuthentication =
        ClientSecretBasic(BuildConfig.CLIENT_SECRET)
}

private const val REDIRECT_URI = "gamedealz://oauth2redirect"
private val SCOPES = listOf("user_info", "coll_read")
