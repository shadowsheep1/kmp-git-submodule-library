import Versions.iosTestTask

object Versions {
    const val iosTestTask = false
    const val kotlin = "1.5.21"

    // Gradle
    const val androidGradlePlugin = "7.0.2"
    const val gradle = "gradle-7.2-bin"

    // Android
    const val compileSdk = 30
    const val minSdk = 23
    const val targetSdk = compileSdk

    const val sqldelight = "1.5.1"
    const val settings = "0.8"
    const val serialization = "1.2.2"
    // https://kotlinlang.org/docs/mobile/concurrency-and-coroutines.html#multithreaded-coroutines
    const val coroutines = "1.5.1-native-mt"
    const val ktor = "1.6.3"
    const val openapigen = "5.2.1"
}

object Deps {
    object kotlin {
        object stdlib {
            val common = "org.jetbrains.kotlin:kotlin-stdlib-common:${Versions.kotlin}"
            val jdk = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
            val android = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
        }

        object test {
            val common = "org.jetbrains.kotlin:kotlin-test-common:${Versions.kotlin}"
            val common_annotations =
                "org.jetbrains.kotlin:kotlin-test-annotations-common:${Versions.kotlin}"
            val jvm = "org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}"
            val junit = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
            val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
        }

        object coroutines {
            val common =
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
            val jdk = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
            //val native =
            //    "org.jetbrains.kotlinx:kotlinx-coroutines-core-native:${Versions.coroutines}"
            val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
        }
    }

    object multiplatformSettings {
        object core {
            val common = "com.russhwolf:multiplatform-settings:${Versions.settings}"
        }

        object test {
            val common = "com.russhwolf:multiplatform-settings-test:${Versions.settings}"
        }
    }

    object sqldelight {
        object runtime {
            val common = "com.squareup.sqldelight:runtime:${Versions.sqldelight}"
            val jdk = "com.squareup.sqldelight:runtime-jvm:${Versions.sqldelight}"
        }

        object driver {
            val ios = "com.squareup.sqldelight:${if (iosTestTask) "ios" else "native"}-driver:${Versions.sqldelight}"
            val android = "com.squareup.sqldelight:android-driver:${Versions.sqldelight}"
            val sqlite = "com.squareup.sqldelight:sqlite-driver:${Versions.sqldelight}"
        }
    }

    /*
    object sqliter {
        val ios = "co.touchlab:sqliter:${Versions.sqliter}"
    }

    object moko {
        object network {
            val common = "dev.icerock.moko:network:${Versions.mokoNetwork}"
        }
    }
    */

    object ktor {
        object core {
            val common = "io.ktor:ktor-client-core:${Versions.ktor}"
            //val native = "io.ktor:ktor-client-core-native:${Versions.ktor}"
            val jvm = "io.ktor:ktor-client-core-jvm:${Versions.ktor}"
        }

        object json {
            val common = "io.ktor:ktor-client-json:${Versions.ktor}"
            val jvm = "io.ktor:ktor-client-json-jvm:${Versions.ktor}"
            //val native = "io.ktor:ktor-client-json-native:${Versions.ktor}"
        }

        object client {
            val okhttp = "io.ktor:ktor-client-okhttp:${Versions.ktor}"
            val android = "io.ktor:ktor-client-android:${Versions.ktor}"
            val ios = "io.ktor:ktor-client-ios:${Versions.ktor}"
        }

        object serialization {
            val common = "io.ktor:ktor-client-serialization:${Versions.ktor}"
            val jvm = "io.ktor:ktor-client-serialization-jvm:${Versions.ktor}"
            //val native = "io.ktor:ktor-client-serialization-native:${Versions.ktor}"
        }

        object mock {
            val common = "io.ktor:ktor-client-mock:${Versions.ktor}"
            //val native = "io.ktor:ktor-client-mock-native:${Versions.ktor}"
            val jvm = "io.ktor:ktor-client-mock-jvm:${Versions.ktor}"
        }

        object logging {
            val jvm = "io.ktor:ktor-client-logging-jvm:${Versions.ktor}"
        }
    }
}