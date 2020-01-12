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

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import de.r4md4c.gamedealz.test.utils.Page

internal class SideMenu : Page() {

    override fun verify() {
        onView(withId(R.id.material_drawer_layout))
            .perform(DrawerActions.open())

        onView(withText(R.string.title_on_going_deals)).run {
            check(matches(isDisplayed()))
            check(isCompletelyAbove(withText(R.string.title_manage_your_watchlist)))
        }

        onView(withText(R.string.title_manage_your_watchlist)).run {
            check(matches(isDisplayed()))
            check(isCompletelyAbove(withText(R.string.miscellaneous)))
        }

        onView(withText(R.string.miscellaneous)).run {
            check(matches(isDisplayed()))
        }

        onView(withText(R.string.change_region)).run {
            check(matches(isDisplayed()))
            check(isCompletelyAbove(withText(R.string.enable_night_mode)))
            check(isCompletelyBelow(withText(R.string.miscellaneous)))
        }

        onView(withText(R.string.enable_night_mode)).run {
            check(matches(isDisplayed()))
            check(isCompletelyBelow(withText(R.string.miscellaneous)))
        }
    }

    fun verifyCountry(country: String) = apply {
        onView(withText(country)).run {
            check(matches(isDisplayed()))
            check(isCompletelyBelow(withText(R.string.change_region)))
        }
    }

    fun verifyLoggedOut() = apply {
        onView(withText(R.string.sign_in)).run {
            check(matches(isDisplayed()))
            check(isCompletelyAbove(withText(R.string.title_on_going_deals)))
        }
    }

    fun verifyKnownUser(username: String) = apply {
        onView(withText(username)).check(matches(isDisplayed()))
    }

    fun verifyUnknownUser() = apply {
        onView(withText(R.string.signed_in)).check(matches(isDisplayed()))
    }

    fun clickAccountPanel(): AccountsListPage {
        onView(withId(R.id.material_drawer_account_header)).perform(click())
        return on()
    }
}


internal class AccountsListPage : Page() {
    override fun verify() {
        onView(withText(R.string.log_out)).check(matches(isDisplayed()))
    }

    fun clickLogout() {
        onView(withText(R.string.log_out)).perform(click())
    }
}
