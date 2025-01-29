enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
  plugins {
    id("org.jetbrains.kotlin.jvm").version("2.1.0")
  }
}
dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention").version("0.9.0")
  id("com.gradle.develocity").version("3.19")
}
rootProject.name = "build-logic"
include("works")
