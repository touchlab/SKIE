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
