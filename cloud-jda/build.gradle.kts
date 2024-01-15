plugins {
    id("cloud-discord.base-conventions")
    id("cloud-discord.publishing-conventions")
}

version = "2.0.0-dev"

dependencies {
    api(libs.cloud.core)
    compileOnly(libs.cloud.annotations)
    compileOnly(libs.jda)
}
