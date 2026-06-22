package co.touchlab.skie.phases.runtime

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.GeneratedBySkieComment

object SwiftRuntimeGenerator : SirPhase {

    context(context: SirPhase.Context)
    override fun isActive(): Boolean = context.run { SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled }

    context(context: SirPhase.Context)
    override suspend fun execute() {
        getSwiftRuntimeFiles().forEach {
            val baseFileContent = it.readText()

            context.namespaceProvider.getSkieNamespaceWrittenSourceFile(it.swiftFileName).content = "// $GeneratedBySkieComment\n\n$baseFileContent"
        }

        val skieSwiftFlowIterator = SkieSwiftFlowIteratorGenerator.generate()
        SupportedFlowRuntimeGenerator.generate(skieSwiftFlowIterator)

        if (context.run { SkieConfigurationFlag.Feature_FlowCombineConvertor.isEnabled }) {
            FlowCombineConversionGenerator.generate()
        }

        if (context.run { SkieConfigurationFlag.Feature_FutureCombineExtension.isEnabled }) {
            FutureCombineExtensionGenerator.generate()
        }

        if (context.run { SkieConfigurationFlag.Feature_SwiftUIObserving.isEnabled }) {
            SwiftUIFlowObservingGenerator.generate()
        }
    }

    private fun getSwiftRuntimeFiles(): List<Resource> =
        Resource("co/touchlab/skie/runtime/index.txt")
            .readText()
            .lines()
            .filter { it.isNotBlank() }
            .map { Resource(it) }

    private val Resource.swiftFileName: String
        get() = this.name.substringAfterLast("/").removeSuffix(".swift")

    private class Resource(val name: String) {

        private val resourceUri = this::class.java.classLoader.getResource(name)
            ?: throw IllegalArgumentException("Resource $name not found.")

        fun readText(): String =
            resourceUri.readText()
    }
}
