plugins {
    id("tests.acceptance-tests-framework")
}

dependencies {
    api(libs.bundles.testing.jvm)

    implementation(libs.kotlinx.coroutines.core)
    implementation(projects.common.configuration.configurationDeclaration)
    implementation(projects.common.util)
    implementation(projects.kotlinCompiler.kotlinCompilerLinkerPlugin)
}
