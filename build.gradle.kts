plugins {
    java
    application
    id("org.beryx.jlink") version "3.0.1"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.javamodularity.moduleplugin") version "1.8.14"
    id("org.jetbrains.kotlin.jvm")
}

group = "io.github.hizumiaoba"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    google()
}

val junitVersion: String = "5.10.1"
val lombokVersion: String = "1.18.30"
val jakartaVersion: String = "2.1.1"

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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("ch.qos.logback:logback-classic:1.5.0")
    implementation("com.google.guava:guava:33.0.0-jre")
    // https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("org.projectlombok:lombok:${lombokVersion}")
    // https://mvnrepository.com/artifact/jakarta.annotation/jakarta.annotation-api
    implementation("jakarta.annotation:jakarta.annotation-api:${jakartaVersion}")
    implementation("com.github.kwhat:jnativehook:2.2.2")

    // test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testImplementation("com.google.truth:truth:1.4.1")

    // annotation processor
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    testAnnotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api:${jakartaVersion}")
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
