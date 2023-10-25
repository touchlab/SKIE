package co.touchlab.skie.kir.element

import co.touchlab.skie.kir.configuration.KirConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor

// Instantiate only in KirProvider
class KirModule(
    val name: String,
    val project: KirProject,
    val descriptor: ModuleDescriptor?,
    val isSkieKotlinRuntime: Boolean,
) : KirClassParent {

    override val classes: MutableList<KirClass> = mutableListOf()

    override val module: KirModule
        get() = this

    override val configuration: KirConfiguration = KirConfiguration(project.configuration)

    init {
        project.modules.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $name"
}
