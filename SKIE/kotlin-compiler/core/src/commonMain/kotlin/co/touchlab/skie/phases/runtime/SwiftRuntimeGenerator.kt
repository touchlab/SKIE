package co.touchlab.skie.phases.runtime

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.GeneratedBySkieComment

object SwiftRuntimeGenerator : SirPhase {

    context(SirPhase.Context)
    override fun isActive(): Boolean = SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled

    context(SirPhase.Context)
    override suspend fun execute() {
        getSwiftRuntimeFiles().forEach {
            val baseFileContent = it.readText()

            namespaceProvider.getSkieNamespaceWrittenSourceFile(it.swiftFileName).content = "// $GeneratedBySkieComment\n\n$baseFileContent"
        }

        val skieSwiftFlowIterator = SkieSwiftFlowIteratorGenerator.generate()
        SupportedFlowRuntimeGenerator.generate(skieSwiftFlowIterator)

        if (SkieConfigurationFlag.Feature_FlowCombineConvertor.isEnabled) {
            FlowCombineConversionGenerator.generate()
        }

        if (SkieConfigurationFlag.Feature_FutureCombineExtension.isEnabled) {
            FutureCombineExtensionGenerator.generate()
        }

        if (SkieConfigurationFlag.Feature_SwiftUIObserving.isEnabled) {
            SwiftUIFlowObservingGenerator.generate()
        }
    }

    private fun getSwiftRuntimeFiles(): List<Resource> = Resource("co/touchlab/skie/runtime/index.txt")
        .readText()
        .lines()
        .filter { it.isNotBlank() }
        .map { Resource(it) }

    private val Resource.swiftFileName: String
        get() = this.name.substringAfterLast("/").removeSuffix(".swift")

    private class Resource(val name: String) {

        private val resourceUri = this::class.java.classLoader.getResource(name)
            ?: throw IllegalArgumentException("Resource $name not found.")

        fun readText(): String = resourceUri.readText()
    }
}
