val clean by tasks.registering {
    dependsOn(":pure-compiler:library:clean")
    dependsOn(":pure-compiler:framework:clean")
}

val buildIosDebug by tasks.registering {
    dependsOn(":pure-compiler:framework:linkDebugFrameworkIosArm64")
}

val buildIosRelease by tasks.registering {
    dependsOn(":pure-compiler:framework:linkReleaseFrameworkIosArm64")
}

val buildMacDebug by tasks.registering {
    dependsOn(":pure-compiler:framework:linkDebugFrameworkMacosArm64")
}

val buildMacRelease by tasks.registering {
    dependsOn(":pure-compiler:framework:linkReleaseFrameworkMacosArm64")
}
