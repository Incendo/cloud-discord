plugins {
    id("cloud-discord.base-conventions")
    id("cloud-discord.publishing-conventions")
}

dependencies {
    api(projects.cloudDiscordCommon)
    implementation(libs.cloud.annotations)

    api(libs.slf4j)
    implementation(libs.discord4j)
}
