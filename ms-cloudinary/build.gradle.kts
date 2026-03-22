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