import com.modrinth.minotaur.TaskModrinthSyncBody
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.spongepowered.asm.gradle.plugins.MixinExtension
import java.time.LocalDateTime

val mod_id: String by extra
val mappings_channel: String by extra
val mc_version: String by extra
val forge_version: String by extra
val registrate_version: String by extra
val create_version: String by extra
val flywheel_version: String by extra
val repository: String by extra
val mod_version: String by extra
val mod_name: String by extra
val mod_author: String by extra
val release_type: String by extra
val modrinth_project_id: String by extra
val curseforge_project_id: String by extra
val jei_version: String by extra
val kubejs_version: String by extra
val overweight_farming_version: String by extra
val farmers_delight_version: String by extra
val blueprint_version: String by extra
val neapolitan_version: String by extra

val localEnv = file(".env").takeIf { it.exists() }?.readLines()?.associate {
    val (key, value) = it.split("=")
    key to value
} ?: emptyMap()

val env = System.getenv() + localEnv
val isCI = env["CI"] == "true"

buildscript {
    dependencies {
        classpath("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    }
}

plugins {
    id("org.sonarqube") version ("3.4.0.2513")
    id("maven-publish")
    id("net.minecraftforge.gradle") version ("5.1.+")
    id("net.darkhax.curseforgegradle") version ("1.0.8")
    id("com.modrinth.minotaur") version ("2.+")
    id("org.jetbrains.kotlin.jvm") version ("1.8.21")
}

apply(plugin = "net.minecraftforge.gradle")
apply(plugin = "kotlin")
apply(plugin = "org.spongepowered.mixin")

configure<MixinExtension> {
    add(sourceSets.main.get(), "${mod_id}.refmap.json")
    config("${mod_id}.mixins.json")
}

val artifactGroup = "com.possible_triangle"
base {
    archivesName.set("$mod_id-forge-$mod_version")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

minecraft {
    mappings(mappings_channel, mc_version)

    runs {
        create("client") {
            workingDirectory(project.file("run"))
        }

        create("server") {
            workingDirectory(project.file("run/server"))
        }

        create("data") {
            workingDirectory(project.file("run"))

            args(
                "--mod",
                mod_id,
                "--all",
                "--output",
                file("src/generated/resources/"),
                "--existing",
                file("src/main/resources")
            )
        }

        forEach {
            it.property("forge.logging.console.level", "debug")

            it.arg("-mixin.config=create.mixins.json")
            it.arg("-mixin.config=flywheel.mixins.json")
            it.arg("-mixin.config=${mod_id}.mixins.json")
            it.property("mixin.env.remapRefMap", "true")
            it.property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")

            it.mods {
                create(mod_id) {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

// Include assets and data from data generators
sourceSets["main"].resources.srcDir("src/generated/resources/")

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
            includeGroup("com.simibubi.create")
            includeGroup("com.jozufozu.flywheel")
            includeGroup("com.tterrag.registrate")
        }
    }
    maven {
        url = uri("https://www.cursemaven.com")
        content {
            includeGroup("curse.maven")
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
        url = uri("https://maven.saps.dev/minecraft")
        content {
            includeGroup("dev.latvian.mods")
        }
    }
    maven {
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
        content {
            includeGroup("thedarkcolour")
        }
    }
}

dependencies {
    // Use the latest version of Minecraft Forge
    minecraft("net.minecraftforge:forge:${mc_version}-${forge_version}")

    compileOnly(fg.deobf("mezz.jei:jei-${mc_version}-common-api:${jei_version}"))
    compileOnly(fg.deobf("mezz.jei:jei-${mc_version}-forge-api:${jei_version}"))
    runtimeOnly(fg.deobf("mezz.jei:jei-${mc_version}-forge:${jei_version}"))

    compileOnly(fg.deobf("com.tterrag.registrate:Registrate:${registrate_version}"))
    implementation(fg.deobf("curse.maven:create-328085:${create_version}"))
    implementation(fg.deobf("com.jozufozu.flywheel:flywheel-forge-${mc_version}:${flywheel_version}"))

    implementation(fg.deobf("curse.maven:farmers-delight-398521:${farmers_delight_version}"))
    implementation(fg.deobf("curse.maven:overweight-farming-591666:${overweight_farming_version}"))

    compileOnly("dev.latvian.mods:kubejs-forge:${kubejs_version}")

    if (!isCI) {
        runtimeOnly(fg.deobf("curse.maven:neapolitan-382016:${neapolitan_version}"))
        runtimeOnly(fg.deobf("com.teamabnormals:blueprint:${mc_version}-${blueprint_version}"))

        //runtimeOnly(fg.deobf("curse.maven:cofh-core-69162:${project.cofh_core_version}"))
        //runtimeOnly(fg.deobf("curse.maven:thermal-foundation-222880:${project.thermal_foundation_version}"))
        //runtimeOnly(fg.deobf("curse.maven:thermal-expansion-69163:${project.thermal_expansion_version}"))
        //runtimeOnly(fg.deobf("curse.maven:thermal-cultivation-271835:${project.thermal_cultivation_version}"))
    }

    implementation("thedarkcolour:kotlinforforge:3.12.0")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

tasks.withType<Jar> {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${mod_id}" }
    }

    manifest {
        attributes(
            mapOf(
                "Specification-Title" to mod_id,
                "Specification-Version" to mod_version,
                "Implementation-Title" to project.name,
                "Implementation-Version" to mod_version,
                "Implementation-Timestamp" to LocalDateTime.now().toString(),
                "MixinConfigs" to "${mod_id}.mixins.json",
            )
        )
    }
}

tasks.jar {
    finalizedBy("reobfJar")
}

tasks.withType<ProcessResources> {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", version)

    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta", "${mod_id}.mixins.json")) {
        expand(
            mapOf(
                "mod_version" to mod_version,
                "mod_name" to mod_name,
                "mod_id" to mod_id,
                "mod_author" to mod_author,
                "repository" to repository,
            )
        )
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${repository}")
            version = version
            credentials {
                username = env["GITHUB_ACTOR"]
                password = env["GITHUB_TOKEN"]
            }
        }
    }
    publications {
        create<MavenPublication>("gpr") {
            groupId = artifactGroup
            artifactId = mod_id
            version = mod_version
            from(components["java"])
        }
    }
}

env["CURSEFORGE_TOKEN"]?.let { token ->
    tasks.register<TaskPublishCurseForge>("curseforge") {
        dependsOn(tasks.jar)

        apiToken = token

        upload(curseforge_project_id, tasks.jar.get().archiveFile).apply {
            changelogType = "html"
            changelog = env["CHANGELOG"]
            releaseType = release_type
            addModLoader("Forge")
            addGameVersion(mc_version)
            displayName = "Version $mod_version"

            addRelation("create", "requiredDependency")
            addRelation("kotlin-for-forge", "requiredDependency")
            addRelation("farmers-delight", "requiredDependency")
            addRelation("kotlin-for-forge", "optionalDependency")
            addRelation("overweight-farming", "optionalDependency")
        }
    }
}

env["MODRINTH_TOKEN"]?.let { modrinthToken ->
    modrinth {
        token.set(modrinthToken)
        projectId.set(modrinth_project_id)
        versionNumber.set(mod_version)
        versionName.set("Version $mod_version")
        changelog.set(env["CHANGELOG"])
        gameVersions.set(listOf(mc_version))
        loaders.set(listOf("forge"))
        versionType.set(release_type)
        uploadFile.set(tasks.jar.get())
        syncBodyFrom.set(project.file("README.md").readText())
        dependencies {
            required.project("ordsPcFz")
            required.project("LNytGWDc")
            optional.project("R2OftAxM")
        }
    }
}

tasks.named("modrinth") {
    dependsOn(tasks.withType<TaskModrinthSyncBody>())
}

sonarqube {
    properties {
        property("sonar.projectVersion", mod_version)
        property("sonar.projectKey", mod_id)
    }
}