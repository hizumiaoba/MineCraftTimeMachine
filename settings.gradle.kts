pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.1.0"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
    id("com.gradle.develocity").version("3.19")
}
develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
    }
}
rootProject.name = "mctimemachine"
include("app")
