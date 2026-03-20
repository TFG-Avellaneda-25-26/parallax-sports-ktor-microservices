val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val koinVersion: String by project

plugins {
    kotlin("jvm") version "2.3.0" apply false
    id("io.ktor.plugin") version "3.4.1" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20" apply false
}

allprojects {
    group = "es.daw.parallaxbot"
    version = "0.0.1"

    extra["ktorVersion"] = ktorVersion
    extra["kotlinVersion"] = kotlinVersion
    extra["logbackVersion"] = logbackVersion
    extra["koinVersion"] = koinVersion

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

    repositories {
        mavenCentral()
    }
}



