package util

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext

internal actual fun <T> runTest(block: suspend CoroutineScope.() -> T): T = kotlinx.coroutines.runBlocking(EmptyCoroutineContext, block)
