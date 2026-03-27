plugins {
    id("tests.library-tests")
}

dependencies {
    testImplementation(projects.kotlinCompiler.kotlinCompilerLinkerPlugin)
    testImplementation(projects.common.configuration.configurationDeclaration)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.gradle.tooling.api)
    testImplementation(libs.slf4j)
    testImplementation(libs.ktor.client.java)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.serialization.kotlinx.json)
}

libraryTests {
    addTest("pureTest") {
        description = "Runs library tests without SKIE."

        systemFlags.add("disableSkie")
    }

    // When creating a lockfile for a new Kotlin version: run Initialize, then Update, then AddTestsDerivedFromDependencies
    // You need to call Initialize multiple times with increasing `run` number, with pauses of several minutes in between each run to avoid Maven Central request throttling.
    // There are currently about 300 pages, so 4 runs per 100 pages are required - if the total number of tests found stops increasing, you can stop.
    addTest("initializeExternalLibrariesLockfile") {
        description = "Searches Maven Central for all KMP iOSArm64 libraries and writes them into the lockfile."

        // Increment this number each run to fetch all pages
        val run = 0

        val numberOfPages = 100

        systemProperties.put("updateLockfile", lockFile.map { it.asFile.absolutePath })

//         systemProperties.put("queryMavenCentral", "")
        systemProperties.put("queryMavenCentral", "useInternalSearch")
        systemProperties.put("queryMavenCentral-fromPage", (run * numberOfPages).toString())
        systemProperties.put("queryMavenCentral-numberOfPages", numberOfPages.toString())

        systemFlags.addAll(
            // Uncomment to reset the lockfile
//             "ignoreLockfile",
            "includeFailedTestsInLockfile",
            "ignoreExpectedFailures",
            "skipDependencyResolution",
        )
    }

    // Consider using onlyIndices=0-1000 to split the Update task into multiple runs as well, as it takes several hours and results are written back only at the end.
    // In such a case, use the incremental lock updating to avoid issues with running the same task multiple times due to changes in test order and overriding of the lockfile.
    addTest("updateExternalLibrariesLockfile") {
        description = "Resolves and compiles all libraries in the lockfile, writing the results back into the lockfile."

//         systemProperties.put("onlyIndices", "0-1000")

        val incrementalLockfile = lockFile.map { it.asFile.absolutePath + "-incremental" }

//         systemProperties.put("updateLockfileIncrementally", incrementalLockfile)
        systemProperties.put("updateLockfile", lockFile.map { it.asFile.absolutePath })

        systemProperties.put("skipTestsInLockfile", incrementalLockfile)

        systemFlags.addAll(
            // Optionally including the failed tests.
//             "includeFailedTestsInLockfile",
            "ignoreDependencyConstraints",
            "ignoreExpectedFailures",
//             "skipKotlinCompilation",
            "disableSkie",
//             "skipSwiftCompilation",
        )
    }

    addTest("addTestsDerivedFromDependenciesToExternalLibrariesLockfile") {
        description = "Converts dependencies in existing lockfile to tests which are then executed and written in the lockfile."

        systemProperties.put("updateLockfile", lockFile.map { it.asFile.absolutePath })

        systemFlags.addAll(
            "convertLibraryDependenciesToTests",
            "skipTestsInLockfile",
//             "includeFailedTestsInLockfile",
            "ignoreExpectedFailures",
//             "skipKotlinCompilation",
            "disableSkie",
//             "skipSwiftCompilation",
//             "onlyUnresolvedVersions",
        )
    }
}
