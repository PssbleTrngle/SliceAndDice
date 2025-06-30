val mod_id: String by extra
val mod_version: String by extra
val mc_version: String by extra
val registrate_version: String by extra
val create_version: String by extra
val ponder_version: String by extra
val flywheel_version: String by extra
val jei_version: String by extra
val kubejs_version: String by extra
val farmers_delight_version: String by extra

plugins {
    id("com.possible-triangle.gradle") version ("0.2.13")
}

withKotlin()

neoforge {
    enableMixins()
    dataGen()
}

configure<BasePluginExtension> {
    archivesName.set("$mod_id-neoforge-${mod_version}")
}

repositories {
    modrinthMaven()
    mavenLocal()

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
}

dependencies {
    modCompileOnly("mezz.jei:jei-${mc_version}-common-api:${jei_version}")
    modCompileOnly("mezz.jei:jei-${mc_version}-neoforge-api:${jei_version}")
    modRuntimeOnly("mezz.jei:jei-${mc_version}-neoforge:${jei_version}")

    modImplementation("com.tterrag.registrate:Registrate:${registrate_version}")
    modImplementation("com.simibubi.create:create-${mc_version}:${create_version}:slim") { isTransitive = false }
    modImplementation("net.createmod.ponder:Ponder-NeoForge-${mc_version}:${ponder_version}")
    modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${mc_version}:${flywheel_version}")
    modRuntimeOnly("dev.engine-room.flywheel:flywheel-neoforge-${mc_version}:${flywheel_version}")

    modImplementation("maven.modrinth:farmers-delight:${farmers_delight_version}")
    //modImplementation("curse.maven:overweight-farming-591666:${overweight_farming_version}")

    compileOnly("dev.latvian.mods:kubejs-neoforge:${kubejs_version}")

    if (!env.isCI) {
        //modRuntimeOnly("curse.maven:neapolitan-382016:${neapolitan_version}")
        //modRuntimeOnly("com.teamabnormals:blueprint:${mc_version}-${blueprint_version}")

        //modRuntimeOnly("curse.maven:cofh-core-69162:${cofh_core_version}")
        //modRuntimeOnly("curse.maven:thermal-foundation-222880:${thermal_foundation_version}")
        //modRuntimeOnly("curse.maven:thermal-expansion-69163:${thermal_expansion_version}")
        //modRuntimeOnly("curse.maven:thermal-cultivation-271835:${thermal_cultivation_version}")
    }
}

tasks.withType<Jar> {
    exclude("screenshots")
    exclude("example_datapack.zip")
}

enablePublishing {
    githubPackages()
}

uploadToCurseforge {
    dependencies {
        required("create")
        optional("farmers-delight")
        optional("overweight-farming")
    }
}

uploadToModrinth {
    dependencies {
        required("LNytGWDc")
        optional("R2OftAxM")
    }

    syncBodyFromReadme()
}

enableSonarQube()
enableSpotless()