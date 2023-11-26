package de.r4md4c.gamedealz.feature.watchlist

import android.Manifest
import android.app.Application
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.io.Closeable

@RunWith(AndroidJUnit4::class)
class RequestNotificationPermissionsTest {

    private val scenario =
        FragmentScenario.launch(Fragment::class.java, initialState = Lifecycle.State.CREATED)

    private val shadowApplication =
        shadowOf(ApplicationProvider.getApplicationContext<Application>())

    @Test
    fun `given permission is granted should return accepted immediately`() = runTest {
        TestFixture(permissionsGranted = true).use { fixture ->
            val testSubject = fixture.build()

            assertTrue(testSubject.requestNotificationPermission())
        }
    }

    @Test
    fun `given permission is not granted when permission is granted should return True`() =
        runTest {
            val testRegistry = object : ActivityResultRegistry() {
                override fun <I : Any?, O : Any?> onLaunch(
                    requestCode: Int,
                    contract: ActivityResultContract<I, O>,
                    input: I,
                    options: ActivityOptionsCompat?
                ) {
                    dispatchResult(requestCode, true)
                }

            }

            TestFixture(permissionsGranted = false, registry = testRegistry).use { fixture ->
                val testSubject = fixture.build()

                val isAcceptedCompletable = CompletableDeferred<Boolean>()
                backgroundScope.launch {
                    isAcceptedCompletable.complete(testSubject.requestNotificationPermission())
                }

                // Move to started to start delivering the result
                scenario.moveToState(Lifecycle.State.STARTED)

                val isAccepted = isAcceptedCompletable.await()
                assertTrue(isAccepted)
            }
        }

    @Test
    fun `given permission is not granted when user disallows permission should return false`() =
        runTest {
            val testRegistry = object : ActivityResultRegistry() {
                override fun <I : Any?, O : Any?> onLaunch(
                    requestCode: Int,
                    contract: ActivityResultContract<I, O>,
                    input: I,
                    options: ActivityOptionsCompat?
                ) {
                    dispatchResult(requestCode, false)
                }

            }

            TestFixture(permissionsGranted = false, registry = testRegistry).use { fixture ->
                val testSubject = fixture.build()

                val isAcceptedCompletable = CompletableDeferred<Boolean>()
                backgroundScope.launch {
                    isAcceptedCompletable.complete(testSubject.requestNotificationPermission())
                }

                // Move to started to start delivering the result
                scenario.moveToState(Lifecycle.State.STARTED)

                val isAccepted = isAcceptedCompletable.await()
                assertFalse(isAccepted)
            }
        }

    private inner class TestFixture(
        private val permissionsGranted: Boolean = false,
        private val registry: ActivityResultRegistry? = null,
    ) : Closeable by scenario {
        fun build(): RequestNotificationPermissions {
            if (permissionsGranted) {
                shadowApplication.grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
            }

            lateinit var testSubject: RequestNotificationPermissions
            scenario.onFragment { f ->
                testSubject = RequestNotificationPermissions(
                    f,
                    registry ?: f.requireActivity().activityResultRegistry
                )
            }
            return testSubject
        }
    }
}
