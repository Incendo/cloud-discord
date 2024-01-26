plugins {
    id("cloud-discord.base-conventions")
    id("cloud-discord.publishing-conventions")
}

version = "2.0.0-beta.1"

dependencies {
    api(libs.cloud.core)
    implementation(libs.javacord)
}
