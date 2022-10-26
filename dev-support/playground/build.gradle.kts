val clean by tasks.registering {
    dependsOn(":playground:kotlin:library:clean")
    dependsOn(":playground:kotlin:framework:clean")
    dependsOn(":playground:swift:clean")
}

val run by tasks.registering {
    dependsOn(":playground:swift:run")
}

tasks.register("cleanRun") {
    dependsOn(run).mustRunAfter(clean)
}

