val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
}

group = "es.daw"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-thymeleaf")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("com.microsoft.playwright:playwright:1.57.0")
    implementation("io.ktor:ktor-client-cio:3.4.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
    implementation("net.dv8tion:JDA:5.2.1")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")

    runtimeOnly("com.microsoft.playwright:driver:1.57.0")
}
