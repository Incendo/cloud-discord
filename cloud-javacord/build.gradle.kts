plugins {
    id("cloud-discord.legacy-module")
}

dependencies {
    api(libs.cloud.core)
    implementation(libs.javacord)
}
