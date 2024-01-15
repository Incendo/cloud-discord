enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("gradle/build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "sonatypeOssSnapshots"
            mavenContent {
                snapshotsOnly()
            }
        }
        maven("https://m2.dv8tion.net/releases") {
            name = "dv8tion"
            mavenContent { releasesOnly() }
        }
    }
}

rootProject.name = "cloud-discord"

include(":cloud-discord-common")
include(":cloud-javacord")
include(":cloud-jda")
include(":cloud-jda5")

include("examples/example-jda5")
findProject(":examples/example-jda5")?.name = "example-jda5"
