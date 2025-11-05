plugins {
    id("cloud-discord.base-conventions")
    application
}

dependencies {
    implementation(projects.cloudJda6)
    implementation(libs.cloud.annotations)
    implementation(libs.logback.core)
    implementation(libs.logback.classic)
    implementation(libs.jda6)
}

application {
    mainClass = "org.incendo.cloud.discord.jda6.example.ExampleBot"
}
