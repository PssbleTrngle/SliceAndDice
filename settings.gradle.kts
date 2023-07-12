val mod_name: String by extra

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.minecraftforge.net/") }
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
        maven { url = uri("https://maven.parchmentmc.org") }
    }
}

rootProject.name = mod_name