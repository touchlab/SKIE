rootProject.name = "dev-support"

pluginManagement {
    includeBuild("dev-support-build-setup")
    includeBuild("../build-setup-gradle-settings")
}

includeBuild("../SKIE") {
    dependencySubstitution {
        substitute(module("co.touchlab.skie:gradle-plugin")).using(project(":gradle:gradle-plugin"))
        substitute(module("co.touchlab.skie:kotlin-compiler-linker-plugin")).using(project(":kotlin-compiler:kotlin-compiler-linker-plugin"))
        substitute(module("co.touchlab.skie:configuration-annotations")).using(project(":common:configuration:configuration-annotations"))
        substitute(module("co.touchlab.skie:runtime-kotlin")).using(project(":runtime:runtime-kotlin"))
    }
}

plugins {
    id("dev.gradle.settings")
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
