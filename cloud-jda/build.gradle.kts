plugins {
    id("cloud-discord.legacy-module")
}

dependencies {
    api(libs.cloud.core)
    api(projects.cloudDiscordCommon)

    compileOnly(libs.cloud.annotations)
    compileOnly(libs.jda)
}
