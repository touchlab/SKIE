package co.touchlab.skie.plugin.generator.internal.util

internal interface SkieCompilationPhase {

    val isActive: Boolean

    fun execute(descriptorProvider: NativeDescriptorProvider)
}
