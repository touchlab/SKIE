package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File

class ExternalLibrariesTestLoader(private val testTmpDir: File) {
    fun loadLibrariesToTest(): List<ExternalLibraryTest> {
        val allLibrariesFile = this::class.java.getResource("/test-input.json")
        val allLibraries = Json.decodeFromString(MapSerializer(String.serializer(), TestInput.serializer()), allLibrariesFile.readText())

        return allLibraries.toList()
            .mapIndexed { index, (library, input) ->
                ExternalLibraryTest(
                    index,
                    library,
                    input,
                    ExpectedTestResult.Success,
                    testTmpDir.resolve("${index}-${library.replace(" ", "-").replace(":", "-")}").toPath(),
                )
            }
    }
}
