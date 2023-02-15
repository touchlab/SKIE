val clean by tasks.registering {
    dependsOn(":skie:ios:dependency:clean")
    dependsOn(":skie:ios:framework:clean")
}

val buildIosDebug by tasks.registering {
    dependsOn(":skie:ios:framework:linkDebugFrameworkIosArm64")
}

val buildIosRelease by tasks.registering {
    dependsOn(":skie:ios:framework:linkReleaseFrameworkIosArm64")
}
