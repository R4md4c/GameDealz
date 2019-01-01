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

@file:Suppress("unused")

package kotlinx.coroutines.android

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import de.r4md4c.commonproviders.coroutines.GameDealzDispatchers
import kotlinx.coroutines.*
import kotlinx.coroutines.internal.MainDispatcherFactory
import java.lang.reflect.Constructor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Dispatches execution onto Android [Handler].
 *
 * This class provides type-safety and a point for future extensions.
 */
@InternalCoroutinesApi
public sealed class HandlerDispatcher : MainCoroutineDispatcher(), Delay {
    /**
     * Returns dispatcher that executes coroutines immediately when it is already in the right handler context
     * (current looper is the same as this handler's looper). See [isDispatchNeeded] documentation on
     * why this should not be done by default.
     *
     * **Note: This is an experimental api.** Semantics of this dispatcher may change in the future.
     */
    @ExperimentalCoroutinesApi
    public abstract override val immediate: HandlerDispatcher
}

@InternalCoroutinesApi
@Keep
internal class AndroidDispatcherFactory : MainDispatcherFactory {

    @InternalCoroutinesApi
    override fun createDispatcher(allFactories: List<MainDispatcherFactory>) =
        HandlerContext(Looper.getMainLooper().asHandler(async = true), "Main")

    @InternalCoroutinesApi
    override fun hintOnError(): String? =
        "For tests Dispatchers.setMain from kotlinx-coroutines-test module can be used"

    @InternalCoroutinesApi
    override val loadPriority: Int
        get() = Int.MAX_VALUE / 2
}

/**
 * Represents an arbitrary [Handler] as a implementation of [CoroutineDispatcher]
 * with an optional [name] for nicer debugging
 */
@InternalCoroutinesApi
@JvmName("from") // this is for a nice Java API, see issue #255
@JvmOverloads
public fun Handler.asCoroutineDispatcher(name: String? = null): HandlerDispatcher =
    HandlerContext(this, name)

private const val MAX_DELAY = Long.MAX_VALUE / 2 // cannot delay for too long on Android

@VisibleForTesting
internal fun Looper.asHandler(async: Boolean): Handler {
    // Async support was added in API 16.
    if (!async || Build.VERSION.SDK_INT < 16) {
        return Handler(this)
    }

    if (Build.VERSION.SDK_INT >= 28) {
        // TODO compile against API 28 so this can be invoked without reflection.
        val factoryMethod = Handler::class.java.getDeclaredMethod("createAsync", Looper::class.java)
        return factoryMethod.invoke(null, this) as Handler
    }

    val constructor: Constructor<Handler>
    try {
        constructor = Handler::class.java.getDeclaredConstructor(
            Looper::class.java,
            Handler.Callback::class.java, Boolean::class.javaPrimitiveType
        )
    } catch (ignored: NoSuchMethodException) {
        // Hidden constructor absent. Fall back to non-async constructor.
        return Handler(this)
    }
    return constructor.newInstance(this, null, true)
}

@InternalCoroutinesApi
@JvmField
@Deprecated("Use Dispatchers.Main instead", level = DeprecationLevel.HIDDEN)
internal val Main: HandlerDispatcher? =
    runCatching { HandlerContext(Looper.getMainLooper().asHandler(async = true), "Main") }.getOrNull()

/**
 * Implements [CoroutineDispatcher] on top of an arbitrary Android [Handler].
 */
@InternalCoroutinesApi
internal class HandlerContext private constructor(
    private val handler: Handler,
    private val name: String?,
    private val invokeImmediately: Boolean
) : HandlerDispatcher(), Delay {
    /**
     * Creates [CoroutineDispatcher] for the given Android [handler].
     *
     * @param handler a handler.
     * @param name an optional name for debugging.
     */
    public constructor(
        handler: Handler,
        name: String? = null
    ) : this(handler, name, false)

    @Volatile
    private var _immediate: HandlerContext? = if (invokeImmediately) this else null

    override val immediate: HandlerContext = _immediate ?: HandlerContext(handler, name, true).also { _immediate = it }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !invokeImmediately || Looper.myLooper() != handler.looper
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        handler.post(block)
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val block = Runnable {
            with(continuation) { resumeUndispatched(Unit) }
        }
        handler.postDelayed(block, timeMillis.coerceAtMost(MAX_DELAY))
        continuation.invokeOnCancellation { handler.removeCallbacks(block) }
    }

    override fun invokeOnTimeout(timeMillis: Long, block: Runnable): DisposableHandle {
        handler.postDelayed(block, timeMillis.coerceAtMost(MAX_DELAY))
        return object : DisposableHandle {
            override fun dispose() {
                handler.removeCallbacks(block)
            }
        }
    }

    override fun toString(): String =
        if (name != null) {
            if (invokeImmediately) "$name [immediate]" else name
        } else {
            handler.toString()
        }

    override fun equals(other: Any?): Boolean = other is HandlerContext && other.handler === handler
    override fun hashCode(): Int = System.identityHashCode(handler)
}

@Volatile
private var choreographer: Choreographer? = null

/**
 * Awaits the next animation frame and returns frame time in nanoseconds.
 */
public suspend fun awaitFrame(): Long {
    // fast path when choreographer is already known
    val choreographer = choreographer
    if (choreographer != null) {
        return suspendCancellableCoroutine { cont ->
            postFrameCallback(choreographer, cont)
        }
    }
    // post into looper thread thread to figure it out
    return suspendCancellableCoroutine { cont ->
        GameDealzDispatchers.Main.dispatch(EmptyCoroutineContext, Runnable {
            updateChoreographerAndPostFrameCallback(cont)
        })
    }
}

private fun updateChoreographerAndPostFrameCallback(cont: CancellableContinuation<Long>) {
    val choreographer = choreographer ?: Choreographer.getInstance()!!.also { choreographer = it }
    postFrameCallback(choreographer, cont)
}

private fun postFrameCallback(choreographer: Choreographer, cont: CancellableContinuation<Long>) {
    choreographer.postFrameCallback { nanos ->
        with(cont) { GameDealzDispatchers.Main.resumeUndispatched(nanos) }
    }
}
