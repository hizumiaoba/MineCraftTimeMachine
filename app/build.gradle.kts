import braid.society.secret.buildlogic.OutputDefinedVersionForGitHubActionsTask
import java.util.*

plugins {
    java
    application
    alias(libs.plugins.jlink)
    alias(libs.plugins.moduleplugin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.javafxpugin)
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.convention.ghaction.version)
}

group = "io.github.hizumiaoba"
version = "1.2.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    google()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("io.github.hizumiaoba.mctimemachine")
    mainClass.set("io.github.hizumiaoba.mctimemachine.MineCraftTimeMachineApplication")
}

javafx {
    version = "17.0.6"
    modules = mutableListOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation(libs.kotlin.stdlib)

    implementation(libs.logback)
    implementation(libs.guava)
    // https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    implementation(libs.sqlite.jdbc)
    implementation(libs.lombok)
    // https://mvnrepository.com/artifact/jakarta.annotation/jakarta.annotation-api
    implementation(libs.jakarta.annotation)
    // https://mvnrepository.com/artifact/com.melloware/jintellitype
    implementation(libs.jintelitype)
    implementation(libs.gh.api) {
        exclude(group = "org.hamcrest")
    }
    implementation(libs.bundles.jackson)
    implementation(libs.okhttp)


    // test dependencies
    testImplementation(libs.bundles.testlibs)
    testRuntimeOnly(libs.jupiter.engine)

    // annotation processor
    annotationProcessor(libs.bundles.annotationProcess)
    testAnnotationProcessor(libs.lombok)
}

tasks.shadowJar {
    minimize()
    archiveFileName.set("${project.name}-all.jar")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    imageZip.set(project.file("${buildDir}/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(
        mutableListOf(
            "--strip-debug",
            "--compress",
            "2",
            "--no-header-files",
            "--no-man-pages"
        )
    )
    launcher {
        name = "MinecraftTimeMachine"
    }
    jpackage {
        appVersion = version.toString()
        if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")) {
            imageOptions.addAll(
                listOf(
                    "--icon",
                    "${projectDir}/src/main/resources/assets/icon.ico"
                )
            )
            installerType = "msi"
            installerOptions.addAll(
                listOf(
                    "--win-per-user-install",
                    "--win-menu-group",
                    "MinecraftTimeMachine",
                    "--win-menu",
                    "--win-upgrade-uuid",
                    "61c4988a-2efe-406c-980c-15ae268d7627",
                    "--vendor",
                    "Secret Society Braid (@hizumiaoba)",
                    "--win-shortcut",
                    "--win-shortcut-prompt"
                )
            )
        }
        if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("mac")) {
            installerType = "pkg"
            installerOptions.addAll(
                listOf(
                    "--vendor",
                    "Secret Society Braid (@hizumiaoba)",
                    "--mac-package-identifier",
                    "io.github.hizumiaoba.mctimemachine",
                    "--mac-package-name",
                    "MCTM",
                )
            )
        }
        if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("linux")) {
            imageOptions.addAll(
                listOf(
                    "--icon",
                    "${projectDir}/src/main/resources/assets/icon.png"
                )
            )
            installerType = "deb"
            installerOptions.addAll(
                listOf(
                    "--vendor",
                    "Secret Society Braid (@hizumiaoba)",
                    "--linux-package-name",
                    "mctimemachine",
                    "--linux-menu-group",
                    "MinecraftTimeMachine",
                    "--linux-shortcut",
                    "--linux-shortcut-directory",
                    "/usr/share/applications",
                    "--linux-shortcut-name",
                    "MinecraftTimeMachine",
                    "--linux-shortcut-icon",
                    "${projectDir}/src/main/resources/assets/icon.png"
                )
            )
        }
    }
}

tasks.jlinkZip {
    group = "distribution"
}

kotlin {
    jvmToolchain(17)
}

tasks.named("clean") {
    dependsOn(gradle.includedBuild("build-logic").task(":works:clean"))
}

tasks.named<OutputDefinedVersionForGitHubActionsTask>("outputDefinedVersionForGitHubActions") {
    version.set(project.version.toString())
}
