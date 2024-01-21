plugins {
    id("cloud-discord.base-conventions")
    id("cloud-discord.publishing-conventions")
}

dependencies {
    api(projects.cloudDiscordCommon)
    implementation(libs.cloud.annotations)

    implementation(libs.discord4j)
}
