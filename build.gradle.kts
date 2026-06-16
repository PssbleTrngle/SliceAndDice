plugins {
    id("com.possible-triangle.core")
    id("com.possible-triangle.neoforge") apply false
}

withKotlin()

subprojects {
    apply(plugin = "com.possible-triangle.core")

    repositories {
        maven {
            url = uri("https://maven.saps.dev/minecraft")
            content {
                includeGroup("dev.latvian.mods")
            }
        }
        maven {
            url = uri("https://maven.blamejared.com/")
            content {
                includeGroup("mezz.jei")
            }
        }
        maven {
            url = uri("https://mvn.devos.one/snapshots")
            content {
                includeGroup("com.tterrag.registrate")
            }
        }
        maven {
            url = uri("https://maven.createmod.net")
            content {
                includeGroup("com.simibubi.create")
                includeGroup("net.createmod.ponder")
                includeGroup("dev.engine-room.flywheel")
            }
        }
        maven {
            url = uri("https://maven.jaackson.me")
            content {
                includeGroup("com.teamabnormals")
            }
        }
        maven {
            url = uri("https://maven.architectury.dev/")
            content {
                includeGroup("dev.architectury")
            }
        }
        maven {
            url = uri("https://maven.latvian.dev/releases")
            content {
                includeGroup("dev.latvian.mods")
            }
        }
        maven {
            url = uri("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
            content {
                includeGroup("fuzs.forgeconfigapiport")
            }
        }
        maven {
            url = uri("https://maven.ryanhcode.dev/releases")
            content {
                includeGroupAndSubgroups("dev.eriksonn")
                includeGroupAndSubgroups("dev.ryanhcode")
                includeGroupAndSubgroups("dev.simulated_team")
            }
        }
        nexus {
            content {
                includeGroup("com.possible-triangle")
                includeGroup("dev.galena")
            }
        }
        maven {
            url = uri("https://maven.teamabnormals.com/")
            content {
                includeGroup("com.teamabnormals")
            }
        }
    }

    upload.maven.nexus()
}

enableSonarQube()
enableSpotless()
