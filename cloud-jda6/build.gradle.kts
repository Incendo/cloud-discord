plugins {
    id("cloud-discord.base-conventions")
    id("cloud-discord.publishing-conventions")
}

dependencies {
    api(projects.cloudDiscordCommon)
    implementation(libs.cloud.annotations)

    implementation(libs.jda6)
    javadocLinks(libs.jda6) {
        isTransitive = false
    }
}

javadocLinks {
    override(libs.jda6, "https://docs.jda.wiki/")
}

configurations.javadocLinksJavadoc {
    exclude("net.dv8tion")
}
