plugins {
    id("com.possible-triangle.fabric")
}

withKotlin()

fabric {
    dataGen()
}

val rawVersion = mod.version.get().replace("-fabric", "")
base {
    archivesName = "${mod.id.get()}-fabric-${rawVersion}"
}

repositories {
    maven {
        url = uri("https://mvn.devos.one/snapshots/")
        content {
            includeGroup("com.simibubi.create")
            includeGroup("io.github.tropheusj")
            includeGroup("com.tterrag.registrate_fabric")
        }
    }

    maven {
        url = uri("https://maven.createmod.net")
        content {
            includeGroup("net.createmod.ponder")
            includeGroup("dev.engine-room.flywheel")
        }
    }

    maven {
        url = uri("https://mvn.devos.one/releases/")
        content {
            includeGroup("io.github.fabricators_of_create.Porting-Lib")
        }
    }

    maven {
        url = uri("https://maven.jamieswhiteshirt.com/libs-release")
        content {
            includeGroup("com.jamieswhiteshirt")
        }
    }

    maven {
        url = uri("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
        content {
            includeGroup("net.minecraftforge")
            includeGroup("fuzs.forgeconfigapiport")
        }
    }

    maven {
        url = uri("https://maven.blamejared.com/")
        content {
            includeGroup("mezz.jei")
        }
    }

    maven {
        url = uri("https://maven.greenhouse.lgbt/releases/")
        content {
            includeGroup("vectorwing")
        }
    }

    nexus("jitpack") {
        content {
            includeGroup("com.github.Chocohead")
        }
    }
}

dependencies {
    modCompileOnly(libs.jei.common.api)
    modCompileOnly(libs.jei.fabric.api)

    modImplementation(libs.registrate)

    modImplementation(libs.create) {
        // exclude("com.jozufozu.flywheel")
    }

    modImplementation(libs.farmers.delight) {
        exclude(group = "net.fabricmc")
    }

    if (!env.isCI) {
        modRuntimeOnly(libs.jei.fabric)
    }
}

tasks.withType<Jar> {
    exclude("screenshots")
    exclude("example_datapack.zip")
}

upload {
    maven {
        nexus()
    }

    forEach {
        versionName = "Fabric $rawVersion"
        dependencies {
            required("create-fabric")
            optional("farmers-delight-refabricated")
        }
    }
}

enableSonarQube()
enableSpotless()
