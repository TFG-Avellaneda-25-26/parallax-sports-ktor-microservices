val koinVersion = rootProject.extra["koinVersion"] as String
val logbackVersion = rootProject.extra["logbackVersion"] as String
val ktorVersion = rootProject.extra["ktorVersion"] as String

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.1"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io")}
}

dependencies {
    implementation(project(":common"))

    implementation("io.ktor:ktor-client-cio-jvm")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

    implementation("com.github.kotlin-telegram-bot:kotlin-telegram-bot:6.2.0") {
        exclude(module = "webhook")
    }

    implementation("io.insert-koin:koin-ktor:${koinVersion}")
    implementation("io.insert-koin:koin-logger-slf4j:${koinVersion}")
    implementation("io.insert-koin:koin-core:${koinVersion}")

    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}