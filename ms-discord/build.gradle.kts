val kotlinVersion = rootProject.extra["kotlinVersion"] as String

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
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-serialization-gson:3.4.1")

    implementation("io.lettuce:lettuce-core:7.5.0.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}