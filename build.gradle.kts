// Top-level build file where you can add configuration options common to all sub-projects/modules.

// https://developer.android.com/studio/build/gradle-tips
// This block encapsulates custom properties and makes them available to all
// modules in the project.
buildscript {
    repositories {
        mavenCentral()
        google()
        maven ( url = "https://repo1.maven.org/maven2" )
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:${Versions.androidGradlePlugin}")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
        classpath ("com.squareup.sqldelight:gradle-plugin:${Versions.sqldelight}")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlin}")
        classpath ("org.openapitools:openapi-generator-gradle-plugin:${Versions.openapigen}")
    }
}

val kotlinPluginId = "org.jetbrains.kotlin.multiplatform"
val hasPlugin = project.plugins.hasPlugin(kotlinPluginId);
println("kmp: $hasPlugin")

allprojects {
    repositories {
        google()
        mavenCentral()
        maven ( url = "https://repo1.maven.org/maven2" )
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}