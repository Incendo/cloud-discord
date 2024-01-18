import gradle.kotlin.dsl.accessors._4170a67d0be8a515d9becde6b6ee87f3.api
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("cloud-discord.base-conventions")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

configure<KotlinJvmProjectExtension> {
    explicitApi()
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    coreLibrariesVersion = libs.versions.kotlin.get()
    target {
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = "17"
                languageVersion = libs.versions.kotlin.get().split(".").take(2).joinToString(".")
                javaParameters = true
            }
        }
    }

    dependencies {
        api(kotlin("stdlib-jdk8"))
    }

    tasks.named("javadocJar", AbstractArchiveTask::class) {
        from(tasks.named("dokkaHtml"))
    }
}
