@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion


plugins {
    alias(libs.plugins.cloche)
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
}

repositories {
    cloche {
        librariesMinecraft()
        mavenNeoforged()
        mavenNeoforgedMeta()
        mavenParchment()
    }
    maven(url = "https://maven.is-immensely.gay/nightly") { name = "Sappho Company"; description = "Cynosure" }
    maven(url = "https://maven.is-immensely.gay/releases") { name = "Sappho Company"; description = "Kritter" }
    maven(url = "https://maven.teamresourceful.com/repository/maven-public/") { name = "Team Resourceful"; description = "Bytecodecs" }
    maven(url = "https://thedarkcolour.github.io/KotlinForForge/") { name = "KotlinForForge" }
    maven(url = "https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") { name = "DevAuth" }
    maven(url = "https://maven.shedaniel.me") { name = "Shedaniel"; description = "REI" }
    maven(url = "https://api.modrinth.com/maven") { name = "Modrinth Maven"; description = "JEI" }

    mavenLocal()
}

val item_viewer: String = providers.gradleProperty("item_viewer").get()
val mod_version: String = providers.gradleProperty("version").get()
val mod_name: String = providers.gradleProperty("mod_name").get()
val mod_id: String = providers.gradleProperty("modid").get()
val devauth_toggle: Boolean = providers.gradleProperty("devauth_toggle").get().toBoolean()

cloche {
    metadata {
        modId = mod_id
        name = mod_name
        description = "Soul for a soul, and capitalism."
        license = "ARR"
        icon = "icon.png"
        url = "https://github.com/MayaqqDev/Embellishment"
        sources = "https://github.com/MayaqqDev/Embellishment"
        author("Mayaqq")
        dependency { modId = "cynosure"; version { start = "1.0.0" } }
    }

    mappings {
        official()
        parchment(libs.versions.parchment)
    }

    singleTarget {
        neoforge {
            mixins.from(file("src/main/embellishment.mixins.json"))
            datagenDirectory.set(file("build/generated/resources/neoforge"))
            loaderVersion = libs.versions.neoforge.get()
            minecraftVersion = libs.versions.minecraft.get()

            metadata {
                modLoader = "kritter"
                loaderVersion {
                    startInclusive = true
                    start = "1"
                }
                blurLogo = false
            }

            data()

            runs {
                data()
                client()
                server()
            }

            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                modApi(libs.cynosure)

                modImplementation(libs.forge.kotlin)

                modCompileOnly(libs.forge.rei)
                modCompileOnly(libs.forge.jei)
                modCompileOnly(libs.forge.emi)

                modApi(libs.forge.kritter)


                when (item_viewer) {
                    "EMI" -> modLocalRuntime(libs.forge.emi)
                    "REI" -> modLocalRuntime(libs.forge.rei)
                    "JEI" -> modLocalRuntime(libs.forge.jei)
                    "disabled" -> {}
                    else -> error("Invalid item viewer for NeoForge: $item_viewer")
                }

                if (devauth_toggle) modLocalRuntime(libs.forge.devauth)
            }
        }
    }
}


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

kotlin {
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_4
        freeCompilerArgs.addAll(
            "-Xjvm-default=all-compatibility",
            "-Xmulti-platform",
            "-Xno-check-actual",
            "-Xexpect-actual-classes",
            "-XXLanguage:+ExpectRefinement"
        )
    }
    jvmToolchain(21)
}