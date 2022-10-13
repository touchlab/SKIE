plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    compileOnly(kotlin("compiler-embeddable"))
}
