plugins {
    kotlin("jvm")
}

dependencies {
    api(libs.swiftPoet)
    api(projects.swiftpackSpec)

    compileOnly(kotlin("compiler-embeddable"))

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation(libs.compileTesting)
    testImplementation(libs.compileTesting.ksp)
}

tasks.test {
    useJUnitPlatform()
}
