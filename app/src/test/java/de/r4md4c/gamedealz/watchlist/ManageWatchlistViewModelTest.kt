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

package de.r4md4c.gamedealz.watchlist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.notification.Notifier
import de.r4md4c.gamedealz.Fixtures
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.state.Event
import de.r4md4c.gamedealz.common.state.FakeStateMachineDelegate
import de.r4md4c.gamedealz.domain.model.ManageWatchlistModel
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import de.r4md4c.gamedealz.domain.usecase.*
import de.r4md4c.gamedealz.test.CoroutinesTestRule
import de.r4md4c.gamedealz.test.TestDispatchers
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ManageWatchlistViewModelTest {

    private val dispatchers: IDispatchers = TestDispatchers

    private val fakeStateMachineDelegate = FakeStateMachineDelegate()

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var getWatchlistUseCase: GetWatchlistToManageUseCase

    @MockK
    private lateinit var getLatestWatchlistCheckDate: GetLatestWatchlistCheckDate

    @MockK
    private lateinit var removeWatcheesUseCase: RemoveWatcheesUseCase

    @MockK
    private lateinit var dateFormatter: DateFormatter

    @MockK
    private lateinit var checkPricesUseCase: CheckPriceThresholdUseCase

    @MockK
    private lateinit var markNotificationAsReadUseCase: MarkNotificationAsReadUseCase

    @MockK
    private lateinit var notifier: Notifier<WatcheeNotificationModel>

    @InjectMockKs
    private lateinit var viewModel: ManageWatchlistViewModel

    @Before
    fun beforeEach() = MockKAnnotations.init(this)

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
            runBlocking {
                coEvery { getWatchlistUseCase.invoke(any()) } returns flowOf(models)
            }
        }

    }
}
