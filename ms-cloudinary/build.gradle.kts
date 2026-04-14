plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.cloudinary)

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}