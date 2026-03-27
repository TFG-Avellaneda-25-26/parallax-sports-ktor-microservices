plugins {
    application
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io")}
}

dependencies {
    implementation(libs.telegram) {
        exclude(module = "webhook")
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}