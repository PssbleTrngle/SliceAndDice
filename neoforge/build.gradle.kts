plugins {
    id("com.possible-triangle.neoforge")
}

neoforge {
    dependOn(project(":api"))

    dataGen {
        owner = project
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
    modImplementation(pack.modrinth.create.dragons.plus)
    modImplementation(pack.modrinth.create.enchantment.industry)
    modImplementation(pack.modrinth.overweight.farming)

    modIncludeCompileOnly(libs.sable.companion)
    modRuntimeOnly(libs.sable) { isTransitive = false }
    modImplementation(libs.create.simulated) { isTransitive = false }
    modImplementation(libs.create.aeronautics) { isTransitive = false }

    modImplementation(libs.oreganized)

    if (!env.isCI) {
        modRuntimeOnly(libs.jei.neoforge)

        modRuntimeOnly(pack.modrinth.neapolitan)
        modRuntimeOnly(pack.modrinth.gallery)

        modRuntimeOnly(pack.modrinth.recipe.modification)
        modRuntimeOnly(pack.modrinth.vegan.delight)
        modRuntimeOnly(pack.curseforge.catalogue)
        modRuntimeOnly(pack.curseforge.configured)
    }
}

upload {
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
