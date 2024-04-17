package co.touchlab.skie.kir.element

import co.touchlab.skie.configuration.ModuleConfiguration

// Instantiate only in KirProvider
class KirModule(
    val name: String,
    val project: KirProject,
    val configuration: ModuleConfiguration,
    val origin: Origin,
) : KirClassParent {

    override val classes: MutableList<KirClass> = mutableListOf()

    override val module: KirModule
        get() = this

    init {
        project.modules.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $name"

    enum class Origin {
        Kotlin, SkieRuntime, SkieGenerated, KnownExternal, UnknownExternal
    }
}
