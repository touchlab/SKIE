pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        repositories {
            google()
            mavenCentral()
            maven("https://maven.pkg.github.com/Touchlab/SwiftPack") {
                name = "gitHub-swiftpack"
                credentials {
                    val githubActor: String? by settings
                    val githubToken: String? by settings
                    username = System.getenv("TL_READ_ACTOR") ?: githubActor
                    password = System.getenv("TL_READ_TOKEN") ?: githubToken
                }
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        repositories {
            google()
            mavenCentral()
            maven("https://maven.pkg.github.com/Touchlab/SwiftPack") {
                name = "gitHub-swiftpack"
                credentials {
                    val githubActor: String? by settings
                    val githubToken: String? by settings
                    username = System.getenv("TL_READ_ACTOR") ?: githubActor
                    password = System.getenv("TL_READ_TOKEN") ?: githubToken
                }
            }
        }
    }
}

rootProject.name = "swiftkt"

include(":example")
include(":example:static")
include(":example:dynamic")

includeBuild("swiftkt-plugin")
