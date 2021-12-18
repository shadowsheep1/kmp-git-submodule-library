package ktor

import io.ktor.client.engine.*
import io.ktor.client.engine.android.*
import kotlinx.coroutines.Dispatchers
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.SocketAddress
import java.security.SecureRandom
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.CoroutineContext

internal actual val ApplicationDispatcher: CoroutineContext = Dispatchers.IO

actual fun clientEngine(timeout: Int): HttpClientEngine {
    //return Android.create() // Default engine
    return Android.create {
        connectTimeout = timeout * 1_000 // timeout secs
        socketTimeout = timeout * 1_000
        requestConfig = {
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
    }
}

actual typealias WeakRef<T> = java.lang.ref.WeakReference<T>