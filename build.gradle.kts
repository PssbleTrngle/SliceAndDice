plugins {
    id("com.possible-triangle.forge")
}

withKotlin()

forge {
    enableMixins()
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
        url = uri("https://maven.tterrag.com/")
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
}

dependencies {
    modCompileOnly(libs.jei.common.api)
    modCompileOnly(libs.jei.forge.api)

    modImplementation(libs.registrate)

    modImplementation(
        variantOf(libs.create) {
            classifier("slim")
        },
    ) {
        isTransitive = false
    }

    modImplementation(libs.ponder)
    modImplementation(libs.flywheel)

    modImplementation(pack.modrinth.overweight.farming)
    modImplementation(pack.modrinth.farmers.delight)
    modCompileOnly(pack.modrinth.create.enchantment.industry)

    if (!env.isCI) {
        modRuntimeOnly(libs.jei.forge)

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
            optional("bCxmmxKN")
        }

        syncBodyFromReadme()
    }
}

enableSonarQube()
enableSpotless()
