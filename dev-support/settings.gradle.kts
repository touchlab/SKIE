rootProject.name = "dev-support"

pluginManagement {
    includeBuild("../build-setup")
    includeBuild("../build-setup-settings")
    includeBuild("../common")
    includeBuild("../skie-gradle")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

plugins {
    id("dev.settings")
}

include(
//    ":analytics",
//    ":ir-inspector",
    ":skie:mac",
    ":skie:mac:framework",
    ":skie:mac:dependency",
    ":skie:mac:swift",
//    ":skie:ios",
//    ":skie:ios:framework",
//    ":skie:ios:dependency",
//    ":skie:ios:swift",
//    ":pure-compiler",
//    ":pure-compiler:dependency",
//    ":pure-compiler:framework",
//    ":pure-compiler:swift",
)
