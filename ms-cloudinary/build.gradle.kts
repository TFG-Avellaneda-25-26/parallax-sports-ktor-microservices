val koinVersion = rootProject.extra["koinVersion"] as String
val logbackVersion = rootProject.extra["logbackVersion"] as String

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))

    implementation("com.cloudinary:cloudinary-http5:2.3.2")

    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-client-cio-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    implementation("io.insert-koin:koin-ktor:${koinVersion}")
    implementation("io.insert-koin:koin-logger-slf4j:${koinVersion}")
    implementation("io.insert-koin:koin-core:${koinVersion}")

    implementation("ch.qos.logback:logback-classic:${logbackVersion}")


    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}