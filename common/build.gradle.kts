val koinVersion = rootProject.extra["koinVersion"] as String

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.insert-koin:koin-ktor:${koinVersion}")
    implementation("io.insert-koin:koin-logger-slf4j:${koinVersion}")
    implementation("io.insert-koin:koin-core:${koinVersion}")

    implementation("io.ktor:ktor-client-cio-jvm")
    implementation("io.ktor:ktor-client-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-core-jvm")
}

kotlin {
    jvmToolchain(21)
}