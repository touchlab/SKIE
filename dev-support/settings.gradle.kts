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

// includeBuild("..") {
//     dependencySubstitution {
//         substitute(module("co.touchlab.skie:kotlin-gradle-plugin-shim-impl")).using(project(":gradle:kotlin-gradle-plugin-shim:kotlin-gradle-plugin-shim-impl"))
//     }
// }

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
