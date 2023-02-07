package co.touchlab.skie.plugin.libraries

import groovy.json.JsonSlurper
import java.io.File

class ExternalLibrariesTestLoader(private val testTmpDir: File) {
    fun loadLibrariesToTest(onlyIndices: Set<Int>): List<ExternalLibraryTest> {
        val allLibrariesFile = this::class.java.getResource("/libraries-to-test.json")
        val allLibraries = JsonSlurper().parse(allLibrariesFile) as Map<String, List<String>>

        val librariesToTest = allLibraries.toList()
            .mapIndexed { index, (library, exportedLibraries) ->
                ExternalLibraryTest(index, library, exportedLibraries)
            }
            .filter { onlyIndices.isEmpty() || it.index in onlyIndices }

        testTmpDir.resolve("tested-libraries.log").writeText(
            librariesToTest.joinToString("\n") { test ->
                "library-${test.index}: ${test.library}"
            }
        )

        return librariesToTest
    }
}
