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

package kotlinx.coroutines.android

import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineExceptionHandler
import java.lang.reflect.Modifier
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private val getter =
    try {
        Thread::class.java.getDeclaredMethod("getUncaughtExceptionPreHandler").takeIf {
            Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers)
        }
    } catch (e: Throwable) {
        null /* not found */
    }

/**
 * Uses Android's `Thread.getUncaughtExceptionPreHandler()` whose default behavior is to log exception.
 * See
 * [here](https://github.com/aosp-mirror/platform_frameworks_base/blob/2efbc7239f419c931784acf98960ed6abc38c3f2/core/java/com/android/internal/os/RuntimeInit.java#L142)
 */
@Keep
internal class AndroidExceptionPreHandler :
    AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        (getter?.invoke(null) as? Thread.UncaughtExceptionHandler)
            ?.uncaughtException(Thread.currentThread(), exception)
    }
}