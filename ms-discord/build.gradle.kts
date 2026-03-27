plugins {
    application
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.serialization.gson)
    implementation(libs.jda)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}