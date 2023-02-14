val clean by tasks.registering {
    dependsOn(":skie:mac:library:clean")
    dependsOn(":skie:mac:framework:clean")
    dependsOn(":skie:mac:swift:clean")
}

val buildMacDebug by tasks.registering {
    dependsOn(":skie:mac:framework:linkDebugFrameworkMacosArm64")
}

val buildMacRelease by tasks.registering {
    dependsOn(":skie:mac:framework:linkReleaseFrameworkMacosArm64")
}

val runSwift by tasks.registering {
    dependsOn(":skie:mac:swift:run")
}
