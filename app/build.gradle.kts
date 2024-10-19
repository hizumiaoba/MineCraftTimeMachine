import java.util.*

plugins {
    java
    application
    alias(libs.plugins.jlink)
    alias(libs.plugins.moduleplugin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.javafxpugin)
    id("org.jetbrains.kotlin.jvm")
}

group = "io.github.hizumiaoba"
version = "1.0.1"

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
    implementation(libs.gh.api)
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
    options.set(mutableListOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "MinecraftTimeMachine"
    }
    jpackage {
        appVersion = version.toString()
        if(System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")) {
          imageOptions.addAll(listOf("--icon", "${projectDir}/src/main/resources/assets/icon.ico"))
          installerOptions.addAll(listOf("--win-per-user-install","--win-dir-chooser", "--win-menu", "--win-shortcut", "--win-shortcut-prompt"))
        }
    }
}

tasks.jlinkZip {
    group = "distribution"
}

kotlin {
    jvmToolchain(17)
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}
