plugins {
    id("cloud-discord.kotlin-conventions")
    id("cloud-discord.publishing-conventions")
}

dependencies {
    api(projects.cloudDiscordCommon)
    api(libs.cloud.kotlin.coroutines)
    api(libs.cloud.kotlin.extensions)
    api(libs.bundles.coroutines)

    implementation(libs.cloud.annotations)
    implementation(libs.kord)

    testImplementation(libs.mockito.kotlin)
}
