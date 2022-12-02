package co.touchlab.skie.plugin.generator.internal.runtime

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import io.outfoxx.swiftpoet.TypeAliasSpec
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

internal class RuntimeGenerator(
    private val skieContext: SkieContext,
    configuration: Configuration,
) : SkieCompilationPhase {

    override val isActive: Boolean = SkieFeature.SuspendInterop in configuration.enabledFeatures

    override fun execute(descriptorProvider: DescriptorProvider) {
        hideKotlinRuntime(descriptorProvider)
        includeSwiftRuntime()
    }

    private fun hideKotlinRuntime(descriptorProvider: DescriptorProvider) {
        skieContext.module.configure {
            descriptorProvider.classDescriptors
                .filter { it.belongsToSkieRuntime }
                .forEach {
                    it.isHiddenFromSwift = true
                }
        }
    }

    private fun includeSwiftRuntime() {
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
