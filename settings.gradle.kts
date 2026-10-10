rootProject.name = "embellishment"

pluginManagement {
    repositories {
        mavenLocal()

        gradlePluginPortal()

        maven(url = "https://maven.msrandom.net/repository/cloche")
        maven(url = "https://maven.msrandom.net/repository/root")
        maven(url = "https://maven.is-immensely.gay/nightly")
    }
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        from(files("libs.versions.toml"))
    }

}