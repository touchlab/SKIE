package co.touchlab.skie.phases.runtime

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirFile

object SwiftRuntimeGenerator : SirPhase {

    context(SirPhase.Context)
    override fun isActive(): Boolean =
        SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags

    context(SirPhase.Context)
    override fun execute() {
        getSwiftRuntimeFiles().forEach {
            sirProvider.getFile(SirFile.skieNamespace, it.swiftFileName).content = it.readText()
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
