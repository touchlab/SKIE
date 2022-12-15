package co.touchlab.skie.plugin.generator.internal.runtime

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase

internal class SwiftRuntimeGenerator(
    private val skieContext: SkieContext,
    configuration: Configuration,
) : SkieCompilationPhase {

    override val isActive: Boolean = SkieFeature.SwiftRuntime in configuration.enabledFeatures

    override fun execute(descriptorProvider: DescriptorProvider) {
        getSwiftRuntimeFiles().forEach {
            skieContext.module.file(it.swiftFileName, it.readText())
        }
    }

    private fun getSwiftRuntimeFiles(): List<Resource> =
        Resource("co/touchlab/skie/runtime/index.txt")
            .readText()
            .lines()
            .filter { it.isNotBlank() }
            .map { Resource(it) }

    private val Resource.swiftFileName: String
        get() = this.name.replace("/", "_").removeSuffix(".swift")

    private class Resource(val name: String) {

        private val resourceUri = this::class.java.classLoader.getResource(name)
            ?: throw IllegalArgumentException("Resource $name not found.")

        fun readText(): String =
            resourceUri.readText()
    }
}
