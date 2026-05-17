@file:Suppress("UnstableApiUsage")

rootProject.name = "ParallaxBot"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":common")
include(":ms-playwright")
include(":ms-discord")
include(":ms-email")
include(":ms-cloudinary")