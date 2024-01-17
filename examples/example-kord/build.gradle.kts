plugins {
    id("cloud-discord.kotlin-conventions")
    application
}

indra {
    javaVersions {
        minimumToolchain(17)
        target(17)
        testWith().set(setOf(17))
    }
}

dependencies {
    implementation(projects.cloudKord)
    implementation(libs.cloud.annotations)
    implementation(libs.cloud.kotlin.coroutines.annotations)
    implementation(libs.kord)
    implementation(libs.kotlin.logging)
    implementation(libs.logback.core)
    implementation(libs.logback.classic)
}

application {
    mainClass = "org.incendo.cloud.discord.kord.example.ExampleBotKt"
}
