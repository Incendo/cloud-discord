plugins {
    id("cloud-discord.legacy-module")
}

dependencies {
    api(libs.cloud.core)
    api(projects.cloudDiscordCommon)

    compileOnly(libs.cloud.annotations)
    implementation(libs.jda)
    javadocLinks(libs.jda) {
        isTransitive = false
    }
}

configurations.javadocLinks {
    exclude("net.dv8tion")
}
