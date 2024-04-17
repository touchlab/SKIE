package co.touchlab.skie.kir.type

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.element.SirClass

enum class SupportedFlow(private val directParent: SupportedFlow?) {
    Flow(null),
    SharedFlow(Flow),
    MutableSharedFlow(SharedFlow),
    StateFlow(SharedFlow),
    MutableStateFlow(StateFlow);

    val coroutinesFlowFqName: String = "kotlinx.coroutines.flow.${name}"

    val requiredVariant: Variant.Required = Variant.Required(this)
    val optionalVariant: Variant.Optional = Variant.Optional(this)

    val variants: List<Variant> = listOf(requiredVariant, optionalVariant)

    fun getCoroutinesKirClass(kirProvider: KirProvider): KirClass =
        kirProvider.getClassByFqName(coroutinesFlowFqName)

    context(SirPhase.Context)
    fun getCoroutinesKirClass(): KirClass = getCoroutinesKirClass(kirProvider)

    sealed interface Variant {

        val kind: SupportedFlow

        fun getCoroutinesKirClass(kirProvider: KirProvider): KirClass =
            kind.getCoroutinesKirClass(kirProvider)

        fun getKotlinKirClass(kirProvider: KirProvider): KirClass

        fun getSwiftClass(sirProvider: SirProvider): SirClass

        context(SirPhase.Context)
        fun getKotlinKirClass(): KirClass = getKotlinKirClass(kirProvider)

        context(SirPhase.Context)
        fun getSwiftClass(): SirClass = getSwiftClass(sirProvider)

        fun isCastableTo(variant: Variant): Boolean

        class Required(override val kind: SupportedFlow) : Variant {

            override fun getKotlinKirClass(kirProvider: KirProvider): KirClass =
                kirProvider.getClassByFqName("co.touchlab.skie.runtime.coroutines.flow.SkieKotlin${kind.name}")

            override fun getSwiftClass(sirProvider: SirProvider): SirClass =
                sirProvider.getClassByFqName(SirFqName(sirProvider.skieModule, "SkieSwift${kind.name}"))

            override fun isCastableTo(variant: Variant): Boolean {
                return kind.isSelfOrChildOf(variant.kind)
            }
        }

        class Optional(override val kind: SupportedFlow) : Variant {

            override fun getKotlinKirClass(kirProvider: KirProvider): KirClass =
                kirProvider.getClassByFqName("co.touchlab.skie.runtime.coroutines.flow.SkieKotlinOptional${kind.name}")

            override fun getSwiftClass(sirProvider: SirProvider): SirClass =
                sirProvider.getClassByFqName(SirFqName(sirProvider.skieModule, "SkieSwiftOptional${kind.name}"))

            override fun isCastableTo(variant: Variant): Boolean {
                if (variant is Required) return false

                return kind.isSelfOrChildOf(variant.kind)
            }
        }
    }

    private fun isSelfOrChildOf(flow: SupportedFlow): Boolean =
        this == flow || (directParent?.isSelfOrChildOf(flow) ?: false)

    companion object {

        val allVariants: List<Variant> = values().flatMap { it.variants }.toList()
    }
}
