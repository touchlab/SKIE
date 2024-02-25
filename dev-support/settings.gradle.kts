rootProject.name = "dev-support"

pluginManagement {
    includeBuild("../build-setup")
    includeBuild("../build-setup-settings")
    includeBuild("../SKIE")
}

includeBuild("../SKIE")

plugins {
    id("dev.settings")
}

fun modules(vararg names: String) {
    names.forEach { name ->
        val path = name.drop(1).split(":")
        val joinedName = path.joinToString("-", prefix = ":")
        include(joinedName)
        project(joinedName).projectDir = path.fold(rootDir) { acc, directoryName ->
            acc.resolve(directoryName)
        }
    }
}

modules(
    ":skie:mac",
    ":skie:mac:framework",
    ":skie:mac:dependency",
    ":skie:mac:swift",
    ":skie:ios",
    ":skie:ios:framework",
    ":skie:ios:dependency",
    ":skie:ios:swift",
    ":pure-compiler",
    ":pure-compiler:dependency",
    ":pure-compiler:framework",
    ":pure-compiler:swift",
)
