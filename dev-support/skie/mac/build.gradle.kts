val clean by tasks.registering {
    dependsOn(":skie:mac:dependency:clean")
    dependsOn(":skie:mac:framework:clean")
    dependsOn(":skie:mac:swift:clean")
}

val buildMacDebug by tasks.registering {
    dependsOn(":skie:mac:framework:linkDebugFrameworkMacosArm64")
}

val buildMacRelease by tasks.registering {
    dependsOn(":skie:mac:framework:linkReleaseFrameworkMacosArm64")
}

val buildSwiftDebug by tasks.registering {
    dependsOn(":skie:mac:swift:buildDebug")
}

val buildSwiftRelease by tasks.registering {
    dependsOn(":skie:mac:swift:buildRelease")
}

val runSwiftDebug by tasks.registering {
    dependsOn(":skie:mac:swift:runDebug")
}

val runSwiftRelease by tasks.registering {
    dependsOn(":skie:mac:swift:runRelease")
}
