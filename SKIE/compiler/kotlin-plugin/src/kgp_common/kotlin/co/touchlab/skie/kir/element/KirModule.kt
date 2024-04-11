package co.touchlab.skie.kir.element

import co.touchlab.skie.configuration.ModuleConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor

// Instantiate only in KirProvider
class KirModule(
    val name: String,
    val project: KirProject,
    val descriptor: ModuleDescriptor?,
    val isSkieKotlinRuntime: Boolean,
    val configuration: ModuleConfiguration,
) : KirClassParent {

    override val classes: MutableList<KirClass> = mutableListOf()

    override val module: KirModule
        get() = this

    init {
        project.modules.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $name"
}
