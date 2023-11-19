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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.notification.Notifier
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.state.Event
import de.r4md4c.gamedealz.domain.model.ManageWatchlistModel
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import de.r4md4c.gamedealz.domain.usecase.CheckPriceThresholdUseCase
import de.r4md4c.gamedealz.domain.usecase.GetLatestWatchlistCheckDate
import de.r4md4c.gamedealz.domain.usecase.GetWatchlistToManageUseCase
import de.r4md4c.gamedealz.domain.usecase.MarkNotificationAsReadUseCase
import de.r4md4c.gamedealz.domain.usecase.RemoveWatcheesUseCase
import de.r4md4c.gamedealz.feature.watchlist.state.FakeStateMachineDelegate
import de.r4md4c.gamedealz.test.CoroutinesTestRule
import de.r4md4c.gamedealz.test.TestDispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.wheneverBlocking

@Ignore("Fix later")
class ManageWatchlistViewModelTest {

    private val dispatchers: IDispatchers = TestDispatchers

    private val fakeStateMachineDelegate = FakeStateMachineDelegate()

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var getWatchlistUseCase: GetWatchlistToManageUseCase

    @Mock
    private lateinit var getLatestWatchlistCheckDate: GetLatestWatchlistCheckDate

    @Mock
    private lateinit var removeWatcheesUseCase: RemoveWatcheesUseCase

    @Mock
    private lateinit var dateFormatter: DateFormatter

    @Mock
    private lateinit var checkPricesUseCase: CheckPriceThresholdUseCase

    @Mock
    private lateinit var markNotificationAsReadUseCase: MarkNotificationAsReadUseCase

    @Mock
    private lateinit var notifier: Notifier<WatcheeNotificationModel>

    private lateinit var viewModel: ManageWatchlistViewModel

    private lateinit var closeable: AutoCloseable

    @Before
    fun beforeEach() {
        closeable = MockitoAnnotations.openMocks(this)
        viewModel = ManageWatchlistViewModel(
            dispatchers = dispatchers,
            getWatchlistUseCase = getWatchlistUseCase,
            getLatestWatchlistCheckDate = getLatestWatchlistCheckDate,
            removeWatcheesUseCase = removeWatcheesUseCase,
            stateMachineDelegate = fakeStateMachineDelegate,
            dateFormatter = dateFormatter,
            checkPricesUseCase = checkPricesUseCase,
            markNotificationAsReadUseCase = markNotificationAsReadUseCase,
            notifier = notifier
        )
    }

    @After
    fun afterEach() {
        closeable.close()
    }

    @Test
    fun `init emit OnShowEmpty event when models are empty`() = runBlockingTest {
        ArrangeBuilder()
            .withWatchlistModels(emptyList())

        viewModel.init()

        assertEquals(fakeStateMachineDelegate.lastEvent, Event.OnShowEmpty)
    }

    @Test
    fun `init emit OnShowEmpty event when models are not empty`() = runBlockingTest {
        ArrangeBuilder()
            .withWatchlistModels(listOf(Fixtures.manageWatchlistModel()))

        viewModel.init()

        assertEquals(fakeStateMachineDelegate.lastEvent, Event.OnLoadingEnded)
    }

    @Test
    fun `it should exclude models that were swiped`() = runBlockingTest {
        ArrangeBuilder()
            .withWatchlistModels((1..10)
                .map { Fixtures.manageWatchlistModel(watcheeModel = Fixtures.watcheeModel(id = it.toLong())) })

        viewModel.onItemSwiped(Fixtures.manageWatchlistModel(watcheeModel = Fixtures.watcheeModel(id = 1)))
        viewModel.init()

        // Assert the same list except the first one since that one got swiped away.
        viewModel.watchlistLiveData.test().assertValue((2..10)
            .map { Fixtures.manageWatchlistModel(watcheeModel = Fixtures.watcheeModel(id = it.toLong())) })
    }

    @Test
    fun `it should include models that were swiped then undone`() = runBlockingTest {
        ArrangeBuilder()
            .withWatchlistModels((1..10)
                .map { Fixtures.manageWatchlistModel(watcheeModel = Fixtures.watcheeModel(id = it.toLong())) })

        viewModel.onItemSwiped(Fixtures.manageWatchlistModel(watcheeModel = Fixtures.watcheeModel(id = 1)))
        viewModel.onItemUndone(Fixtures.manageWatchlistModel(watcheeModel = Fixtures.watcheeModel(id = 1)))
        viewModel.init()

        viewModel.watchlistLiveData.test().assertValue((1..10)
            .map { Fixtures.manageWatchlistModel(watcheeModel = Fixtures.watcheeModel(id = it.toLong())) })
    }

    inner class ArrangeBuilder {

        fun withWatchlistModels(models: List<ManageWatchlistModel>) = apply {
            wheneverBlocking { getWatchlistUseCase.invoke(any()) } doReturn flowOf(models)
        }

    }
}
