package util

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext

/**
 * https://github.com/ktorio/ktor/issues/895
 * https://github.com/Kotlin/kotlinx.coroutines/issues/770
 * https://youtrack.jetbrains.com/issue/KTOR-691
 */

internal actual fun <T> runTest(block: suspend CoroutineScope.() -> T): T = kotlinx.coroutines.runBlocking(EmptyCoroutineContext, block)
