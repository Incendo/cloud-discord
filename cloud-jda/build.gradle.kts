plugins {
    id("cloud-discord.base-conventions")
    id("cloud-discord.publishing-conventions")
}

version = "2.0.0-SNAPSHOT"

dependencies {
    api(libs.cloud.core)
    api(projects.cloudDiscordCommon)

    compileOnly(libs.cloud.annotations)
    compileOnly(libs.jda)
}
