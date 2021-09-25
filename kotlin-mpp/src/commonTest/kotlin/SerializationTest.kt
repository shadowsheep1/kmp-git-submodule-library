import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class Data(val a: Int, val b: String = "42")

class SerializationTest {
    @Test
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    fun testSerialization() {
        // Json also has .Default configuration which provides more reasonable settings,
        // but is subject to change in future versions
        val json = Json.Default
        // serializing objects
        val jsonData = json.encodeToString(Data(42))
        // serializing lists
        val jsonList = json.encodeToString(listOf(Data(42)))
        println(jsonData) // {"a": 42, "b": "42"}
        println(jsonList) // [{"a": 42, "b": "42"}]

        // parsing data back
        val obj = json.decodeFromString<Data>(
            """{"a":42}"""
        ) // b is optional since it has default value
        println(obj) // Data(a=42, b="42")
        assertEquals(obj.a, 42)
    }
}