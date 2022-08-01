rootProject.name = "SwiftGen"

include(
    ":acceptance-tests:framework",
    ":acceptance-tests",
    ":playground:irinspector",
    ":playground:kotlin",
    ":playground:swift",
)

includeBuild("core")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.github.com/Touchlab/SwiftPack") {
            name = "gitHub-swiftpack"
            credentials {
                val githubActor: String? by settings
                val githubToken: String? by settings
                username = System.getenv("GITHUB_ACTOR") ?: githubActor
                password = System.getenv("GITHUB_TOKEN") ?: githubToken
            }
        }
        maven("https://maven.pkg.github.com/Touchlab/SwiftKt") {
            name = "gitHub-swiftkt"
            credentials {
                val githubActor: String? by settings
                val githubToken: String? by settings
                username = System.getenv("GITHUB_ACTOR") ?: githubActor
                password = System.getenv("GITHUB_TOKEN") ?: githubToken
            }
        }
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
        maven("https://maven.pkg.github.com/Touchlab/SwiftKt") {
            name = "gitHub-swiftkt"
            credentials {
                val githubActor: String? by settings
                val githubToken: String? by settings
                username = System.getenv("GITHUB_ACTOR") ?: githubActor
                password = System.getenv("GITHUB_TOKEN") ?: githubToken
            }
        }
    }
}