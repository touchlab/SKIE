package co.touchlab.skie.plugin.generator.internal.coroutines.flow

import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.MutableDescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.allExposedMembers
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.util.flow.SupportedFlow
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.createFunction
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.util.createValueParameter
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irUnit
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType

internal class FlowGenericArgumentStubGenerator(
    private val skieContext: SkieContext,
    private val descriptorProvider: MutableDescriptorProvider,
    private val declarationBuilder: DeclarationBuilder,
) : SkieCompilationPhase {

    override val isActive: Boolean = SkieFeature.CoroutinesInterop in skieContext.configuration.enabledFeatures

    override fun runObjcPhase() {
        val allFlowArguments = descriptorProvider.allExposedMembers.flatMap { it.getAllFlowArgumentClasses() } +
            descriptorProvider.exposedClasses.flatMap { it.getAllFlowArgumentClasses() }

        val nonExposedFlowArguments = allFlowArguments
            .distinct()
            .filter { it !in descriptorProvider.exposedClasses }

        exposeClasses(nonExposedFlowArguments)
    }

    private fun exposeClasses(classDescriptors: List<ClassDescriptor>) {
        val dummyFunction = generateDummyFunction(classDescriptors)

        dummyFunction.removeFromSwift()
    }

    private fun generateDummyFunction(parameters: List<ClassDescriptor>): FunctionDescriptor =
        declarationBuilder.createFunction(
            name = "Skie_FlowGenericArgumentExports",
            namespace = declarationBuilder.getCustomNamespace("Skie_FlowGenericArgumentExports"),
            annotations = Annotations.EMPTY,
        ) {
            valueParameters = parameters.mapIndexed { index: Int, classDescriptor: ClassDescriptor ->
                createValueParameter(
                    owner = descriptor,
                    name = Name.identifier("p${index}"),
                    index = index,
                    type = classDescriptor.defaultType,
                )
            }

            body = {
                irBlockBody {
                    +irReturn(irUnit())
                }
            }
        }

    private fun FunctionDescriptor.removeFromSwift() {
        skieContext.module.configure {
            this.swiftModel.visibility = SwiftModelVisibility.Removed
        }
    }
}

private fun ClassDescriptor.getAllFlowArgumentClasses(): List<ClassDescriptor> =
    declaredTypeParameters.flatMap { it.getAllFlowArgumentClasses() }

private fun CallableMemberDescriptor.getAllFlowArgumentClasses(): List<ClassDescriptor> =
    typeParameters.flatMap { it.getAllFlowArgumentClasses() } +
        valueParameters.flatMap { it.type.getAllFlowArgumentClasses() } +
        (returnType?.getAllFlowArgumentClasses() ?: emptyList())

// Sacrifices some completeness for simplicity - otherwise it would have to check all usages of the type parameter.
private fun TypeParameterDescriptor.getAllFlowArgumentClasses(): List<ClassDescriptor> =
    upperBounds.flatMap { it.getAllReferencedClasses() }

private fun KotlinType.getAllFlowArgumentClasses(visitedTypes: Set<KotlinType> = emptySet()): List<ClassDescriptor> {
    if (this in visitedTypes) return emptyList()

    val nextVisitedTypes = visitedTypes + this

    return if (isSupportedFlow) {
        arguments.flatMap { it.type.getAllReferencedClasses(nextVisitedTypes) }
    } else {
        arguments.flatMap { it.type.getAllFlowArgumentClasses(nextVisitedTypes) }
    }
}

private fun KotlinType.getAllReferencedClasses(visitedTypes: Set<KotlinType> = emptySet()): List<ClassDescriptor> {
    val thisClass = listOfNotNull(constructor.declarationDescriptor as? ClassDescriptor)

    if (this in visitedTypes) return thisClass

    val nextVisitedTypes = visitedTypes + this

    return thisClass + arguments.flatMap { it.type.getAllReferencedClasses(nextVisitedTypes) }
}

private val KotlinType.isSupportedFlow: Boolean
    get() = SupportedFlow.from(this) != null
