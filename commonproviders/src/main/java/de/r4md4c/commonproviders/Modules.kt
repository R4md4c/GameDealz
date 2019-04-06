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

package de.r4md4c.commonproviders

import android.app.Activity
import de.r4md4c.commonproviders.appcompat.AppCompatProvider
import de.r4md4c.commonproviders.appcompat.ApplicationAppCompatProvider
import de.r4md4c.commonproviders.configuration.AndroidConfigurationImpl
import de.r4md4c.commonproviders.configuration.ConfigurationProvider
import de.r4md4c.commonproviders.coroutines.GameDealzDispatchers
import de.r4md4c.commonproviders.date.AndroidDateFormatter
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.date.DateProvider
import de.r4md4c.commonproviders.date.JavaDateProvider
import de.r4md4c.commonproviders.preferences.AndroidSharedPreferencesProvider
import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.commonproviders.res.AndroidResourcesProvider
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.IDispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

const val FOR_ACTIVITY = "for_activity"
const val FOR_APPLICATION = "for_application"

val COMMON_PROVIDERS = module {

    single<IDispatchers> { GameDealzDispatchers }

    factory<AppCompatProvider> { ApplicationAppCompatProvider() }

    factory<ConfigurationProvider> { AndroidConfigurationImpl(androidContext()) }

    factory<DateProvider> { JavaDateProvider() }

    single<SharedPreferencesProvider> { AndroidSharedPreferencesProvider(androidContext()) }

    factory<ResourcesProvider>(name = FOR_APPLICATION) { AndroidResourcesProvider(androidContext()) }

    factory<ResourcesProvider>(name = FOR_ACTIVITY) { (activity: Activity) -> AndroidResourcesProvider(activity) }

    factory<DateFormatter> { AndroidDateFormatter(androidContext()) }
}