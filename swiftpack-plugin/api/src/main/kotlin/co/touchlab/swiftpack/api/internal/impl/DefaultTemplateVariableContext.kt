package co.touchlab.swiftpack.api.internal.impl

import co.touchlab.swiftpack.api.internal.InternalTemplateVariableContext
import co.touchlab.swiftpack.spec.reference.KotlinEnumEntryReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import co.touchlab.swiftpack.spec.reference.KotlinDeclarationReference
import co.touchlab.swiftpack.spec.reference.KotlinTypeReference
import co.touchlab.swiftpack.spec.module.SWIFTPACK_TEMPLATE_VARIABLE_PREFIX
import co.touchlab.swiftpack.spec.module.SwiftTemplateVariable
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.SelfTypeName
import java.util.concurrent.atomic.AtomicInteger

internal class DefaultTemplateVariableContext: InternalTemplateVariableContext {
    private val referenceCounter = AtomicInteger(-1)

    private val referencedSymbols = mutableMapOf<KotlinDeclarationReference.Id, SwiftTemplateVariable<*>>()
    override val variables: Collection<SwiftTemplateVariable<*>> = referencedSymbols.values

    override fun KotlinTypeReference.Id.swiftTemplateVariable(): DeclaredTypeName {
        val ref = getVariable(this) { name, referenceId ->
            SwiftTemplateVariable.TypeReference(name, referenceId)
        }
        return DeclaredTypeName.typeName(".${ref.name.value}")
    }

    override fun KotlinPropertyReference.Id.swiftTemplateVariable(): PropertySpec {
        val variable = getVariable(this) { name, referenceId ->
            SwiftTemplateVariable.PropertyReference(name, referenceId)
        }
        // TODO: Should we provide the builder with a real type?
        return PropertySpec.builder(variable.name.value, SelfTypeName.INSTANCE).build()
    }

    override fun KotlinFunctionReference.Id.swiftTemplateVariable(): FunctionSpec {
        val variable = getVariable(this) { name, referenceId ->
            SwiftTemplateVariable.FunctionReference(name, referenceId)
        }
        // TODO: Should we provide the builder with a real return type and parameters?
        return FunctionSpec.builder(variable.name.value).build()
    }

    override fun KotlinEnumEntryReference.Id.swiftTemplateVariable(): PropertySpec {
        val variable = getVariable(this) { name, referenceId ->
            SwiftTemplateVariable.EnumEntryReference(name, referenceId)
        }
        // TODO: Should we provide the builder with a real type?
        return PropertySpec.builder(variable.name.value, SelfTypeName.INSTANCE).build()
    }

    private fun <ID: KotlinDeclarationReference.Id> getVariable(
        referenceId: ID,
        variableFactory: (name: SwiftTemplateVariable.Name, referenceId: ID) -> SwiftTemplateVariable<ID>,
    ): SwiftTemplateVariable<ID> {
        return referencedSymbols.getOrPut(referenceId) {
            val name = SwiftTemplateVariable.Name("$SWIFTPACK_TEMPLATE_VARIABLE_PREFIX${referenceCounter.incrementAndGet()}")
            variableFactory(name, referenceId)
        } as SwiftTemplateVariable<ID>
    }
}
