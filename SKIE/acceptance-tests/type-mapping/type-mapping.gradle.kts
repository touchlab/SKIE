plugins {
    id("tests.type-mapping-tests")
    id("utility.experimental.context-receivers")
}

dependencies {
    testImplementation(projects.kotlinCompiler.kotlinCompilerLinkerPlugin)
    testImplementation(projects.common.configuration.configurationDeclaration)

    testImplementation(kotlin("test"))

    testDependencies(projects.acceptanceTests.typeMapping.testDependencies.regularDependency)
    testExportedDependencies(projects.acceptanceTests.typeMapping.testDependencies.exportedDependency)
}
