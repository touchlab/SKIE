import co.touchlab.touchlabtools.DocusaurusOssTemplateExtension

plugins {
    alias(libs.plugins.docusaurusOssTemplate)
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}

tasks.register("cleanAll") {
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}

docusaurusOss {
    destination.set(projectDir)
}

task("replaceSkieVersions") {
    group = "DocusaurusOssTemplate"

    doLast {
        val directory = getDocusaurusDirectory()

        val baseSkieVersion = (project.property("LATEST_GITHUB_VERSION") as String).split("-")[0];

        val replacements = listOf(
            "{{SKIE_VERSION}}" to baseSkieVersion,
            "{{SKIE_VERSION_PREVIEW}}" to "$baseSkieVersion-preview.1.8.20",
        )

        directory.recursiveReplace(replacements)
    }
}

fun getDocusaurusDirectory(): File {
    val extension = extensions.getByType<DocusaurusOssTemplateExtension>()

    val destinationDir = extension.destination.get()

    return File(destinationDir, "docs")
}

fun File.recursiveReplace(replacements: List<Pair<String, String>>) {
    walkTopDown()
        .filter { it.isFile && it.extension in listOf("md", "mdx") }
        .forEach { file ->
            val text = file.readText()

            val replacedText = replacements.fold(text) { acc, (from, to) ->
                acc.replace(from, to)
            }

            file.writeText(replacedText)
        }
}
