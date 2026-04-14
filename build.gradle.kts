import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.libs

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dependency.analysis)
}

allprojects {
    group = "es.daw.parallaxbot"
    version = "0.0.1"

    repositories {
        mavenCentral()
    }

    tasks.withType<ProcessResources>().configureEach {
        if (project.name != "common") {
            project.evaluationDependsOn(":common")

            val sharedResources = project(":common").layout.projectDirectory.dir("src/main/resources")

            from(sharedResources) {
                include("shared-data.conf")
                include("logback.xml")
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }
    }
}

subprojects {
    val libs = rootProject.extensions.getByType<LibrariesForLibs>()

    apply(plugin = libs.plugins.kotlin.jvm.get().pluginId)
    apply(plugin = libs.plugins.kotlin.serialization.get().pluginId)

    dependencies {

        if (project.path != ":common") {
            apply(plugin = libs.plugins.ktor.get().pluginId)
            add("implementation", project(":common"))
            add("implementation", libs.ktor.server.netty)
            add("implementation", libs.ktor.server.content.negotiation)
            add("implementation", libs.koin.ktor)
            add("implementation", libs.koin.logger.slf4j)
            add("implementation", libs.logback.classic)
        }

        if (project.path != ":ms-playwright" && project.path != ":ms-cloudinary") {
            add("implementation", libs.redis.lettuce)
            add("implementation", libs.kotlin.coroutines.core)
        }

        add("implementation", libs.ktor.server.core)
        add("implementation", libs.ktor.client.cio)
        add("implementation", libs.ktor.client.content.negotiation)
        add("implementation", libs.ktor.serialization.json)
        add("implementation", libs.koin.core)
    }
}