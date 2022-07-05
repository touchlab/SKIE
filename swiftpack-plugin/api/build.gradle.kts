plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    api("io.outfoxx:swiftpoet:1.4.2")
    api(project(":swiftpack-spec"))

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.9")
}

tasks.test {
    useJUnitPlatform()
}

