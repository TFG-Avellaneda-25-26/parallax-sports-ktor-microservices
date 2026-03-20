val koinVersion = rootProject.extra["koinVersion"] as String
val logbackVersion = rootProject.extra["logbackVersion"] as String

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))

    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

    implementation("com.google.api-client:google-api-client:2.9.0")
    implementation("com.google.apis:google-api-services-gmail:v1-rev20260112-2.0.0")
    implementation("jakarta.mail:jakarta.mail-api:2.1.5")
    implementation("org.eclipse.angus:jakarta.mail:2.0.5")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("io.ktor:ktor-client-cio-jvm")

    implementation("io.insert-koin:koin-ktor:${koinVersion}")
    implementation("io.insert-koin:koin-logger-slf4j:${koinVersion}")
    implementation("io.insert-koin:koin-core:${koinVersion}")

    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}