package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.element.SirClass
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType

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

    sealed interface Variant {

        val owner: SupportedFlow

        fun getKotlinKirClass(kirProvider: KirProvider): KirClass

        fun getSwiftClass(sirProvider: SirProvider): SirClass

        context(SirPhase.Context)
        fun getKotlinKirClass(): KirClass = getKotlinKirClass(kirProvider)

        context(SirPhase.Context)
        fun getSwiftClass(): SirClass = getSwiftClass(sirProvider)

        fun isCastableTo(variant: Variant): Boolean

        class Required(override val owner: SupportedFlow) : Variant {

            override fun getKotlinKirClass(kirProvider: KirProvider): KirClass =
                kirProvider.getClassByFqName("co.touchlab.skie.runtime.coroutines.flow.SkieKotlin${owner.name}")

            override fun getSwiftClass(sirProvider: SirProvider): SirClass =
                sirProvider.getClassByFqName(SirFqName(sirProvider.skieModule, "SkieSwift${owner.name}"))

            override fun isCastableTo(variant: Variant): Boolean {
                return owner.isSelfOrChildOf(variant.owner)
            }
        }

        class Optional(override val owner: SupportedFlow) : Variant {

            override fun getKotlinKirClass(kirProvider: KirProvider): KirClass =
                kirProvider.getClassByFqName("co.touchlab.skie.runtime.coroutines.flow.SkieKotlinOptional${owner.name}")

            override fun getSwiftClass(sirProvider: SirProvider): SirClass =
                sirProvider.getClassByFqName(SirFqName(sirProvider.skieModule, "SkieSwiftOptional${owner.name}"))

            override fun isCastableTo(variant: Variant): Boolean {
                if (variant is Required) return false

                return owner.isSelfOrChildOf(variant.owner)
            }
        }
    }

    private fun isSelfOrChildOf(flow: SupportedFlow): Boolean =
        this == flow || (directParent?.isSelfOrChildOf(flow) ?: false)

    companion object {

        fun from(type: KotlinType): SupportedFlow? =
            (type.constructor.declarationDescriptor as? ClassDescriptor)?.let { from(it) }

        private fun from(classDescriptor: ClassDescriptor): SupportedFlow? {
            val classFqName = classDescriptor.fqNameSafe.asString()

            return values().firstOrNull { it.coroutinesFlowFqName == classFqName }
        }
    }
}
