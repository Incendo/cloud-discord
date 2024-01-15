plugins {
    id("cloud-discord.base-conventions")
    application
}

dependencies {
    implementation(projects.cloudJda5)
    implementation(libs.cloud.annotations)
    implementation(libs.logback.core)
    implementation(libs.logback.classic)
    implementation(libs.jda5)
}

application {
    mainClass = "org.incendo.cloud.discord.jda5.example.ExampleBot"
}
