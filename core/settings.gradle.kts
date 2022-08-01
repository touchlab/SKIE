rootProject.name = "SwiftGen-plugin"

include(
    ":compiler-plugin",
    ":gradle-plugin",
)

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.github.com/Touchlab/SwiftPack") {
            name = "gitHub-swiftpack"
            credentials {
                val githubActor: String? by settings
                val githubToken: String? by settings
                username = System.getenv("GITHUB_ACTOR") ?: githubActor
                password = System.getenv("GITHUB_TOKEN") ?: githubToken
            }
        }
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}