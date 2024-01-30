plugins {
    id("cloud-discord.legacy-module")
}

dependencies {
    api(libs.cloud.core)
    api(libs.log4j)
    implementation(libs.javacord)
}
