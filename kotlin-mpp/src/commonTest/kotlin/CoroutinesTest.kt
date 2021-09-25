import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import util.runTest
import kotlin.test.Test

//https://youtrack.jetbrains.com/issue/KT-22228
//expect fun <T> runTest(block: suspend () -> T)

class CoroutinesTest {
    suspend fun _hello() = coroutineScope {
        launch {
            delay(1000)
            println("Kotlin Coroutines World!")
        }
        println("Hello")
    }

    @Test
    fun sayHello() {
        runTest {
            _hello()
        }
    }
}