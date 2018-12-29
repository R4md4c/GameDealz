package de.r4md4c.gamedealz.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.whileSelect
import java.util.concurrent.TimeUnit

fun <T> ReceiveChannel<T>.debounce(
    scope: CoroutineScope,
    time: Long,
    unit: TimeUnit = TimeUnit.MILLISECONDS
): ReceiveChannel<T> =
    Channel<T>(capacity = Channel.CONFLATED).also { channel ->
        scope.launch {
            var value = receive()
            whileSelect {
                onTimeout(unit.toMillis(time)) {
                    channel.offer(value)
                    value = receive()
                    true
                }
                onReceive {
                    value = it
                    true
                }
            }
        }
    }