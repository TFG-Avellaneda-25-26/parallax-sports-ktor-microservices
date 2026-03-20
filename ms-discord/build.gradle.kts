val kotlinVersion = rootProject.extra["kotlinVersion"] as String
val logbackVersion = rootProject.extra["logbackVersion"] as String
val koinVersion = rootProject.extra["koinVersion"] as String

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.1"
    //id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
//
    implementation("io.ktor:ktor-client-cio-jvm")

    implementation(project(":common"))
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
//
    implementation("net.dv8tion:JDA:6.3.1")
//
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")
    implementation("io.insert-koin:koin-core:${koinVersion}")
//
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-serialization-gson:3.4.1")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}