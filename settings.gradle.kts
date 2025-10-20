pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.2.20"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.gradle.develocity").version("3.19")
}
develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/terms-of-service")
        termsOfUseAgree.set("yes")
    }
}
rootProject.name = "mctimemachine"
include("app")
includeBuild("build-logic")
