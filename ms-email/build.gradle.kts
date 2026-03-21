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

    implementation("io.ktor:ktor-client-cio-jvm")

    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    implementation("io.lettuce:lettuce-core:7.5.0.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}