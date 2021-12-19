// https://guides.gradle.org/migrating-build-logic-from-groovy-to-kotlin/
// https://plugins.gradle.org/plugin/org.jetbrains.kotlin.multiplatform
// https://kotlinlang.org/docs/reference/using-gradle.html
// https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

val compileJvm = false
val compileMacOS = false

plugins {
    // https://developer.android.com/studio/projects/android-library
    id("com.android.library")
    kotlin("multiplatform")
    // https://github.com/JetBrains/kotlin-native/blob/master/COCOAPODS.md
    kotlin("native.cocoapods")
    id("com.squareup.sqldelight")
    id("kotlinx-serialization")
    id("org.openapi.generator")

}

println("compileSdkVersion: ${Versions.compileSdk}")

// https://github.com/gradle/kotlin-dsl-samples/issues/163
android {
    compileSdkVersion(Versions.compileSdk)
    defaultConfig {
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    //https://github.com/sellmair/mpp-playground/blob/connectedAndroidTest-not-executed/mpp-lib/build.gradle.kts
    /*
    sourceSets {
        getByName("androidTest").java.srcDir(file("src/androidTest/kotlin"))
    }
    */

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDirs("src/androidMain/kotlin")
            res.srcDirs("src/androidMain/res")
        }
        getByName("test") {
            java.srcDirs("src/androidTest/kotlin")
            res.srcDirs("src/androidTest/res")
        }
    }

    // This is more stuff... just copied for references
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions.unitTests.isIncludeAndroidResources = true
}

group = "it.shadowsheep.kotlin.mpp"
version = "0.0.1"

//apply (plugin = "maven-publish")

openApiGenerate {
    inputSpec.set(file("src/openapi.json").path)
    templateDir.set(file("swagger-template/kotlin-client").path)
    //outputDir.set("$buildDir/generated")
    generatorName.set("kotlin")
    library.set("multiplatform")
    verbose.set(false)
    packageName.set("it.shadowsheep.kotlin.mpp.app.client")
}

// https://github.com/cashapp/sqldelight#supported-dialects
sqldelight {
    database("AppDb") {
        //package name used for the generated MyDatabase.kt
        packageName = "it.shadowsheep.kotlin.mpp.db"
        // An array of folders where the plugin will read your '.sq' and '.sqm' files.
        // The folders are relative to the existing source set so if you specify ["db"],
        // the plugin will look into 'src/main/db'
        // Defaults to ["sqldelight"] (src/main/sqldelight)
        sourceFolders = listOf("sqldelight")
        // The directory where to store '.db' schema files relative to the root of the project.
        // These files are used to verify that migrations yield a database with the latest schema.
        // Defaults to null so the verification tasks will not be created.
        schemaOutputDirectory = file("build/dbs")
        // Optionally specify schema dependencies on other gradle projects
        //dependency project(':OtherProject')
    }
    // For native targets, whether sqlite should be automatically linked.
    // Defaults to true.
    //linkSqlite = false
}

// https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#default-project-layout
kotlin {
    //select iOS target platform depending on the Xcode environment variables
    val podTarget = project.findProperty("kotlin.native.cocoapods.target")
    println("--------> pod target $podTarget")
    println("--------> sdk_name ${System.getenv("SDK_NAME")}")

    val iosTarget: (String) -> KotlinNativeTarget = when {
        System.getenv("SDK_NAME")?.startsWith("iphoneos") == true -> ::iosArm64
        System.getenv("NATIVE_ARCH")?.startsWith("arm") == true -> ::iosSimulatorArm64
        else -> ::iosX64
    }

    iosTarget("ios")

    targets.matching { it.platformType.name == "native" }.all {
        // https://github.com/JetBrains/kotlin-native/issues/3208
        val target = this as KotlinNativeTarget
        target.binaries
            .matching { it is Framework }
            .all {
                val framework = this as Framework
                println("->> NativeBinary Framework <<-")
                println("\t - name: ${framework.name}")
                println("\t - baseName: ${framework.baseName}")

                // https://github.com/russhwolf/multiplatform-settings/blob/v0.4/sample/shared/build.gradle.kts
                export(Deps.multiplatformSettings.core.common)
                framework.transitiveExport = true
                framework.embedBitcode = Framework.BitcodeEmbeddingMode.BITCODE

                println("\t - embedBitcode: ${framework.embedBitcode}")
                println("\t - transitiveExport: ${framework.transitiveExport}")
                println("->> NativeBinary Framework <<-")
            }

        // https://kotlinlang.org/docs/reference/whatsnew14.html#objective-c-generics-support-by-default
        /*
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink>().configureEach {
            kotlinOptions.freeCompilerArgs += "-Xobjc-generics"
        }
        */

        compilations.forEach {it as KotlinNativeCompilation
            println("compilation $it")
            // For SQLDelight --> https://github.com/cashapp/sqldelight/issues/1442
            //it.extraOpts("-linker-options", "-lsqlite3")
            //it.extraOpts("-Xobjc-generics")
        }
    }

    cocoapods {
        // Configure fields required by CocoaPods.
        summary = "Mobile and More Kotlin/Native module"
        homepage = "https://www.shadowsheep.it"
    }

    android()
    //jvm("android")

    println("----> Deps: ${Deps.kotlin.stdlib.common}")

    // To include open api client code autogenearated by gradle plugin
    sourceSets["commonMain"]
        .kotlin
        .srcDirs("$buildDir/generate-resources/main/src/commonMain/kotlin")

    // How to exclude a package from the library (a way to)
    sourceSets["commonMain"]
        .kotlin
        .exclude("it/shadowsheep/yap/mpp/foo/**")

    sourceSets["commonMain"].dependencies {
        implementation(kotlin("stdlib-common"))
        // SqlDelight
        implementation(Deps.sqldelight.runtime.common)
        // Multiplatform Settings
        api(Deps.multiplatformSettings.core.common)
        // Serialization
        //implementation(Deps.serialization.common)
        // Coroutines
        implementation(Deps.kotlin.coroutines.common)
        // Ktor
        // https://ktor.io/clients/http-client/multiplatform.html
        api(Deps.ktor.core.common)
        api(Deps.ktor.json.common)
        api(Deps.ktor.serialization.common)
    }

    sourceSets["commonTest"].dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
        // Multiplatform Settings
        implementation(Deps.multiplatformSettings.test.common)
        // SqlDelight
        implementation(Deps.sqldelight.runtime.common)
        // Ktor
        implementation(Deps.ktor.mock.common)
    }

    sourceSets["androidMain"].dependencies {
        implementation(kotlin("stdlib"))
        // SQLDelight
        implementation(Deps.sqldelight.driver.android)
        // Serialization
        //implementation(Deps.serialization.common)
        // Coroutines
        implementation(Deps.kotlin.coroutines.android)
        // Ktor
        api(Deps.ktor.client.android)
        //api(Deps.ktor.client.okhttp))
        api(Deps.ktor.core.common)
        api(Deps.ktor.json.jvm)
        api(Deps.ktor.serialization.jvm)
    }

    sourceSets["androidTest"].dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-junit"))
        implementation("com.android.support.test:runner:1.0.2")
        // SQLDelight
        implementation(Deps.sqldelight.driver.sqlite)
        // Coroutines
        implementation(Deps.kotlin.coroutines.jdk)
        implementation(Deps.kotlin.coroutines.android)
        // Ktor
        implementation(Deps.ktor.mock.jvm)
    }

    sourceSets["iosMain"].dependencies {
        // SQLDelight
        implementation(Deps.sqldelight.driver.ios)
        // Ktor
        api(Deps.ktor.client.ios)
        // CrashKios - https://github.com/touchlab/CrashKiOS
        api("co.touchlab:crashkios:0.3.1")
    }

    sourceSets["iosTest"].dependencies {
        // Ktor
        //api(Deps.ktor.mock.native)
    }

    // https://kotlinlang.org/docs/reference/experimental.html
    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.Experimental")
                /*
                    w: ATTENTION!
                    This build uses unsafe internal compiler arguments:

                    -XXLanguage:+InlineClasses
                */
                //enableLanguageFeature("InlineClasses")
                progressiveMode = true
            }
        }
    }
}

if (Versions.iosTestTask) {
    task("iosTest") {
        dependsOn("linkDebugTestIos")
        doLast {
            val testBinaryPath =
                (kotlin.targets["ios"] as KotlinNativeTarget).binaries.getTest("DEBUG")
                    .outputFile.absolutePath
            exec {
                println("testBinaryPath $testBinaryPath")
                //commandLine("xcrun", "simctl", "spawn", "--standalone", "iPhone Xʀ", testBinaryPath)
                commandLine("xcrun", "simctl", "spawn", "--standalone", "iPhone 11", testBinaryPath)
            }
        }
    }
}

val setupLibraryVersion by tasks.creating(Sync::class) {
    println("Version $version")
    println("Group $group") // I still don't know why this is null (if I use rootProject.group then I cannot use it as a library)
    println("Root dir: ${rootProject.rootDir}")

    rootProject.allprojects.forEach {
        println("project: $it")
    }

    var targetDir = File(
        "${rootProject.rootDir}",
        "kotlin-mpp/src/commonMain/kotlin/it/shadowsheep/yap/mpp"
    )

    val regex = Regex("\"[^\"]*\"")

    var versionFile = File(targetDir, "LibraryVersion.kt")
    if (versionFile.exists()) {
        println("LibraryVersion iOS $versionFile")
        changeVersion(versionFile, regex)
    } else {
        targetDir = File(
            "${rootProject.rootDir}",
            "kotlin-mpp/kotlin-mpp/src/commonMain/kotlin/it/shadowsheep/yap/mpp"
        )
        versionFile = File(targetDir, "LibraryVersion.kt")
        if (versionFile.exists()) {
            println("LibraryVersion Android $versionFile")
            changeVersion(versionFile, regex)
        } else {
            println("LibraryVersion $versionFile DOES NOT EXITS")
        }
    }
}

//tasks["check"].dependsOn("iosTest")
tasks["build"].dependsOn(setupLibraryVersion)
//tasks["preBuild"].dependsOn(tasks["openApiGenerate"])
tasks["compileKotlinMetadata"].enabled = false // Same as https://youtrack.jetbrains.com/issue/KTOR-1379, but for runBlocking

fun changeVersion(versionFile: File, regex: Regex) {
    versionFile.also {
        println("File $it")
        it.writeText(regex.replace(it.readText(), "\"$version\""))
        println(it.readText())
    }
}