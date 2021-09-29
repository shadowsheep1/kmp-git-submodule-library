pluginManagement {
  resolutionStrategy {
    eachPlugin {
      println("requested plugin: ${requested.id.id}:${requested.version}")
    }
  }
}

rootProject.name = "kotlin-mpp-library"

include("kotlin-mpp")

