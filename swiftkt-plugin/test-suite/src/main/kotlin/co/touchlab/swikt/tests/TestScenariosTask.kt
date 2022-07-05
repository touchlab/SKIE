package co.touchlab.swikt.tests

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.util.visibleName

abstract class TestScenariosTask: DefaultTask() {
    init {
        description = "List of scenarios to test the Swikt plugin in the current environment"
        group = "verification"
    }

    @TaskAction
    fun printScenarios() {
        val frameworkKinds = listOf("Dynamic", "Static")
        val buildTypes = NativeBuildType.values().map { it.visibleName.capitalized() }.sorted()
        val bitcodeEmbeddingModes = BitcodeEmbeddingMode.values().map { it.visibleName.capitalized() }
        val targets = KonanTarget.predefinedTargets.values.filter { it.family.isAppleFamily }

        println("Binary type | Build type | Bitcode | Target")
        frameworkKinds.forEach { frameworkKind ->
            buildTypes.forEach { buildType ->
                bitcodeEmbeddingModes.forEach { bitcodeEmbeddingMode ->
                    targets.forEach { target ->
                        println("- [ ] $frameworkKind, $buildType, $bitcodeEmbeddingMode, $target")
                    }
                }
            }
        }
    }
}

