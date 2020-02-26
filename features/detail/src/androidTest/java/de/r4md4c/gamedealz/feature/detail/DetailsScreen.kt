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

package de.r4md4c.gamedealz.feature.detail

import android.view.View
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KTextView
import org.hamcrest.Matcher

internal class DetailsScreen : Screen<DetailsScreen>() {
    val addToWatchlistFab = KImageView { withId(R.id.addToWatchList) }
    val recycler = KRecyclerView({
        withId(R.id.content)
    }, itemTypeBuilder = {
        itemType(::PriceDetailItem)
        itemType(::PriceHeaderItem)
    })
}

class PriceDetailItem(parent: Matcher<View>) : KRecyclerItem<PriceDetailItem>(parent) {
    val shop: KTextView = KTextView(parent) { withId(R.id.shop) }
}

class PriceHeaderItem(parent: Matcher<View>) : KRecyclerItem<PriceHeaderItem>(parent) {
    val header: KTextView = KTextView(parent) { withId(R.id.header) }
}
