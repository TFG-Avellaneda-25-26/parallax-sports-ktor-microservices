plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.thymeleaf)
}

kotlin {
    jvmToolchain(21)
}