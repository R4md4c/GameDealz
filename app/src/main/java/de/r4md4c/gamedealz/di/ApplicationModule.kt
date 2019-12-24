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

package de.r4md4c.gamedealz.di

import android.net.Uri
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.r4md4c.gamedealz.network.di.URL_IS_THERE_ANY_DEAL
import net.openid.appauth.AuthorizationServiceConfiguration

@Module(includes = [ApplicationBindsModule::class])
object ApplicationModule {

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
}
