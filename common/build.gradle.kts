plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.thymeleaf)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.kotlin.coroutines.slf4j)
    implementation(libs.logstash.logback.encoder)
}

kotlin {
    jvmToolchain(21)
}