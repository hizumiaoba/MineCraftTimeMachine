plugins {
  `java-library`
  `java-gradle-plugin`
  id("org.jetbrains.kotlin.jvm")
}

gradlePlugin {
  plugins {
    register("OutputDefinedVersionForGitHubActions") {
      id = "convention.ghactions.version"
      implementationClass = "braid.society.secret.buildlogic.OutputDefinedVersionForGitHubActionsPlugin"
    }
  }
}

repositories {
  mavenCentral()
  google()
}

group = "braid.society.secret.buildlogic"
