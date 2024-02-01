plugins {
    id("cloud-discord.base-conventions")
    id("cloud-discord.publishing-conventions")
}

dependencies {
    api(projects.cloudDiscordCommon)
    implementation(libs.cloud.annotations)

    implementation(libs.jda5)
    javadocLinks(libs.jda5) {
        isTransitive = false
    }
}

javadocLinks {
    override(libs.jda5, "https://ci.dv8tion.net/job/JDA5/javadoc/")
}

configurations.javadocLinksJavadoc {
    exclude("net.dv8tion")
}
