import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

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
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        languageVersion = KotlinVersion.fromVersion(
            libs.versions.kotlin.get().split(".").take(2).joinToString(".")
        )
        javaParameters = true
    }
    coreLibrariesVersion = libs.versions.kotlin.get()

    dependencies {
        "api"(kotlin("stdlib-jdk8"))
    }

    tasks.named("javadocJar", AbstractArchiveTask::class) {
        from(tasks.named("dokkaHtml"))
    }
}
