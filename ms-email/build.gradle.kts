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

    implementation(libs.google.api.client)
    implementation(libs.google.api.service.gmail)
    implementation(libs.jakarta.mail.angus)
    implementation(libs.ktor.server.thymeleaf)

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}