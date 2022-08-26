import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)

    alias(libs.plugins.swiftpack)
    alias(libs.plugins.swiftkt)
    id("co.touchlab.swiftgen")
}

kotlin {
    macosX64()
    macosArm64()

    targets.withType<KotlinNativeTarget> {
        binaries {
            framework {
                isStatic = true
                baseName = "Kotlin"
            }
        }
    }

    val commonMain by sourceSets.getting {
        dependencies {
            implementation("co.touchlab.swiftgen:api")
        }
    }

    val macosMain by sourceSets.creating {
        dependsOn(commonMain)
    }

    val macosArm64Main by sourceSets.getting {
        dependsOn(macosMain)
    }

    val macosX64Main by sourceSets.getting {
        dependsOn(macosMain)
    }
}

val printArmGeneratedSwift by tasks.register("printArmGeneratedSwift") {
    doLast {
        val generatedSwiftDirectory =
            layout.buildDirectory.dir("generated/swiftpack-expanded/releaseFramework/macosArm64").get().asFile

        printGeneratedSwift(generatedSwiftDirectory)
    }
}

val printX64GeneratedSwift by tasks.register("printX64GeneratedSwift") {
    doLast {
        val generatedSwiftDirectory =
            layout.buildDirectory.dir("generated/swiftpack-expanded/releaseFramework/macosX64").get().asFile

        printGeneratedSwift(generatedSwiftDirectory)
    }
}

val linkReleaseFrameworkMacosArm64 by tasks.getting {
    finalizedBy(printArmGeneratedSwift)
}

val linkReleaseFrameworkMacosX64 by tasks.getting {
    finalizedBy(printX64GeneratedSwift)
}

fun printGeneratedSwift(path: File) {
    val generatedSwift = path.listFiles()?.joinToString("\n") {
        "------ ${it.name} ------\n" + it.readText()
    }

    println("---------------- Generated Swift ----------------")
    print(generatedSwift)
}