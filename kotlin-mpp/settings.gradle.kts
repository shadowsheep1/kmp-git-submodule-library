pluginManagement {
  plugins {
    kotlin("multiplatform") version("1.5.21")
    // https://github.com/JetBrains/kotlin-native/blob/master/COCOAPODS.md
    kotlin("native.cocoapods") version("1.5.21")
  }
  resolutionStrategy {
    eachPlugin {
      println("requested plugin: ${requested.id.id}:${requested.version}")
    }
  }
}

rootProject.name = "kotlin-mpp"
