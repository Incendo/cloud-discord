plugins {
    id("cloud-discord.base-conventions")
    id("cloud-discord.publishing-conventions")
}

version = "2." + rootProject.version.toString().substringAfter("1.")
