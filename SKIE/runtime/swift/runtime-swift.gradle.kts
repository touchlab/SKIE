plugins {
    id("skie.runtime.swift")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Runtime - Swift"
    description = "Swift part of the SKIE runtime. It's used to facilitate certain features of SKIE."
    publishSources = true
}

val createResourcesIndex by tasks.registering {
    val resourcesProvider = sourceSets.main.map { it.resources.files }

    val outputFileProvider = sourceSets.main.map {
        it.output.resourcesDir!!.resolve("co/touchlab/skie/runtime/index.txt")
    }

    inputs.files(resourcesProvider)
    outputs.file(outputFileProvider)

    doLast {
        val indexContent = resourcesProvider.get().joinToString("\n") { sourceSets.main.get().resourceName(it) }

        val outputFile = outputFileProvider.get()
        outputFile.parentFile.mkdirs()
        outputFile.writeText("$indexContent\n")
    }
}

tasks.named("processResources") {
    dependsOn(createResourcesIndex)
}

fun SourceSet.resourceName(file: File): String {
    val baseResourcesPaths = this.resources.srcDirs

    val baseResourcesFolder = baseResourcesPaths.first { file.startsWith(it) }

    return file.relativeTo(baseResourcesFolder).path
}
