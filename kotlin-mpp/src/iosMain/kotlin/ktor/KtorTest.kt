package ktor

import io.ktor.client.engine.*
import io.ktor.client.engine.ios.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import platform.Foundation.setValue
import kotlin.coroutines.CoroutineContext

import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

internal actual val ApplicationDispatcher: CoroutineContext = MainDispatcher()

internal class MainDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatch_async(dispatch_get_main_queue()) { block.run() }
    }
}

actual fun clientEngine(timeout: Int): HttpClientEngine {
    //Ios.create() // Default engine
    return Ios.create {
        /**
         * Configure native NSUrlRequest.
         */
        configureRequest { // this: NSMutableURLRequest
            setAllowsCellularAccess(true)
            setTimeoutInterval(timeout.toDouble()) // 5 sec
        }
    }
}

actual typealias WeakRef<T> = kotlin.native.ref.WeakReference<T>