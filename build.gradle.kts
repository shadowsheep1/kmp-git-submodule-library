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
        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs") as org.gradle.accessors.dm.LibrariesForLibs
        classpath(libs.bundles.gradlePlugins)
        classpath(kotlin("gradle-plugin", libs.versions.kotlin.get()))
    }
}

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