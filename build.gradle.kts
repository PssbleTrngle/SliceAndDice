plugins {
    id("com.possible-triangle.neoforge")
}

withKotlin()

neoforge {
    dataGen()
}

base {
    archivesName = "${mod.id.get()}-forge-${mod.version.get()}"
}

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
        }
    }
}

dependencies {
    modCompileOnly(libs.jei.common.api)
    modCompileOnly(libs.jei.neoforge.api)

    modApi(libs.registrate)

    modIncludeCompileOnly(libs.atmosphere.api)
    modRuntimeOnly(libs.atmosphere)

    modApi(
        variantOf(libs.create) {
            classifier("slim")
        },
    ) {
        isTransitive = false
    }

    modApi(libs.ponder)
    modApi(libs.flywheel)

    modImplementation(pack.modrinth.farmers.delight)
    modCompileOnly(pack.modrinth.create.enchantment.industry)
    modCompileOnly(pack.modrinth.overweight.farming)

    modIncludeCompileOnly(libs.sable.companion)
    modRuntimeOnly(libs.sable) { isTransitive = false }
    modImplementation(libs.create.simulated) { isTransitive = false }
    modImplementation(libs.create.aeronautics) { isTransitive = false }

    if (!env.isCI) {
        modRuntimeOnly(libs.jei.neoforge)

        modRuntimeOnly(pack.modrinth.blueprint)
        modRuntimeOnly(pack.modrinth.neapolitan)
        modRuntimeOnly(pack.modrinth.gallery)

        modRuntimeOnly(pack.modrinth.recipe.modification)
        modRuntimeOnly(pack.modrinth.vegan.delight)
        modRuntimeOnly(pack.curseforge.catalogue)
        modRuntimeOnly(pack.curseforge.configured)
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

    curseforge {
        dependencies {
            required("create")
            optional("farmers-delight")
            optional("create-enchantment-industry")
            optional("overweight-farming")
        }
    }

    modrinth {
        dependencies {
            required("LNytGWDc")
            optional("R2OftAxM")
            optional("JWGBpFUP")
        }

        syncBodyFromReadme()
    }
}

enableSonarQube()
enableSpotless()
