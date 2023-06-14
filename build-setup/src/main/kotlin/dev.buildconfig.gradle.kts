plugins {
    id("com.github.gmazzo.buildconfig")
}

buildConfig {
    packageName(("${project.group}.${project.name}").replace("-", "_"))
}
