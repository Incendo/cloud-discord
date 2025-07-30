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
    override(libs.jda6, "https://ci.dv8tion.net/job/JDA5/javadoc/")
    // ^ Leaving as JDA5 for now since they don't have a Javadoc for JDA6 just yet
}

configurations.javadocLinksJavadoc {
    exclude("net.dv8tion")
}
