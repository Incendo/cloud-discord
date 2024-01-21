plugins {
    id("cloud-discord.base-conventions")
    application
}

dependencies {
    implementation(projects.cloudDiscord4j)
    implementation(libs.cloud.annotations)
    implementation(libs.logback.core)
    implementation(libs.logback.classic)
    implementation(libs.discord4j)
}

application {
    mainClass = "org.incendo.cloud.discord.discord4j.example.ExampleBot"
}
