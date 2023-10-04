package co.touchlab.skie.phases.other

import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.MutableDescriptorProvider
import co.touchlab.skie.kir.allExposedMembers
import co.touchlab.skie.kir.irbuilder.createFunction
import co.touchlab.skie.kir.irbuilder.util.createValueParameter
import co.touchlab.skie.phases.ClassExportPhase
import co.touchlab.skie.phases.util.StatefulSirPhase
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.swiftmodel.SwiftModelVisibility
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irUnit
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.replaceArgumentsWithStarProjections

class ExtraClassExportPhase(
    private val context: ClassExportPhase.Context,
) : ClassExportPhase {

    private val descriptorProvider = context.descriptorProvider

    context(ClassExportPhase.Context)
    override fun execute() {
        val additionallyExportedClasses = getAllAdditionallyExportedClasses()

        val stubFunction = generateStubFunction(additionallyExportedClasses)

        stubFunction.removeFromSwift()
    }

    private fun getAllAdditionallyExportedClasses(): Set<ClassDescriptor> {
        val originallyExportedClasses = descriptorProvider.exposedClasses.toSet()
        val allExportedClasses = originallyExportedClasses.toMutableSet()

        do {
            val lastIterationResult = allExportedClasses.toSet()

            allExportedClasses.addClassesForExport()

            val newlyDiscoveredClasses = allExportedClasses - lastIterationResult

            descriptorProvider.registerExportedClasses(newlyDiscoveredClasses)
        } while (newlyDiscoveredClasses.isNotEmpty())

        return allExportedClasses - originallyExportedClasses
    }

    private fun MutableSet<ClassDescriptor>.addClassesForExport() {
        addClassesForExportFromFlowArguments()
        addClassesForExportFromSealedHierarchies()
    }

    private fun MutableSet<ClassDescriptor>.addClassesForExportFromFlowArguments() {
        if (SkieConfigurationFlag.Feature_CoroutinesInterop !in context.skieConfiguration.enabledConfigurationFlags) {
            return
        }

        val allFlowArguments = descriptorProvider.allExposedMembers.flatMap { it.getAllFlowArgumentClasses() } +
                descriptorProvider.exposedClasses.flatMap { it.getAllFlowArgumentClasses() }

        val allExposableFlowArguments = allFlowArguments.filter { descriptorProvider.isExposable(it) }

        this.addAll(allExposableFlowArguments)
    }

    private fun MutableSet<ClassDescriptor>.addClassesForExportFromSealedHierarchies() {
        val allExportedSealedChildren = descriptorProvider.exposedClasses.getAllExportedSealedChildren()

        this.addAll(allExportedSealedChildren)
    }

    private fun ClassDescriptor.getAllExportedSealedChildren(): List<ClassDescriptor> {
        if (!context.configurationProvider.getConfiguration(this, SealedInterop.ExportEntireHierarchy)) {
            return emptyList()
        }

        val topLevelSealedChildren = sealedSubclasses.filter { descriptorProvider.isExposable(it) }

        return topLevelSealedChildren + topLevelSealedChildren.getAllExportedSealedChildren()
    }

    private fun Collection<ClassDescriptor>.getAllExportedSealedChildren(): List<ClassDescriptor> =
        this.flatMap { it.getAllExportedSealedChildren() }

    private fun generateStubFunction(exportedClasses: Collection<ClassDescriptor>): FunctionDescriptor =
        context.declarationBuilder.createFunction(
            name = "skieTypeExports",
            namespace = context.declarationBuilder.getCustomNamespace("__SkieTypeExports"),
            annotations = Annotations.EMPTY,
        ) {
            valueParameters = exportedClasses.mapIndexed { index: Int, classDescriptor: ClassDescriptor ->
                createValueParameter(
                    owner = descriptor,
                    name = Name.identifier("p${index}"),
                    index = index,
                    type = classDescriptor.defaultType.replaceArgumentsWithStarProjections(),
                )
            }

            body = {
                irBlockBody {
                    +irReturn(irUnit())
                }
            }
        }

    private fun FunctionDescriptor.removeFromSwift() {
        context.doInPhase(FinalizePhase) {
            this@removeFromSwift.swiftModel.kotlinSirFunction.visibility = SirVisibility.Removed
        }
    }

    object FinalizePhase : StatefulSirPhase()
}

private fun MutableDescriptorProvider.registerExportedClasses(exportedClasses: Collection<ClassDescriptor>) {
    mutate {
        exportedClasses.forEach {
            registerExposedDescriptor(it)
        }
    }
}

private fun ClassDescriptor.getAllFlowArgumentClasses(): List<ClassDescriptor> =
    declaredTypeParameters.flatMap { it.getAllFlowArgumentClasses() }

private fun CallableMemberDescriptor.getAllFlowArgumentClasses(): List<ClassDescriptor> =
    typeParameters.flatMap { it.getAllFlowArgumentClasses() } +
            valueParameters.flatMap { it.type.getAllFlowArgumentClasses() } +
            (returnType?.getAllFlowArgumentClasses() ?: emptyList()) +
            (contextReceiverParameters + listOfNotNull(
                dispatchReceiverParameter,
                extensionReceiverParameter
            )).flatMap { it.getAllFlowArgumentClasses() }

// Sacrifices some completeness for simplicity - otherwise it would have to check all usages of the type parameter.
private fun TypeParameterDescriptor.getAllFlowArgumentClasses(): List<ClassDescriptor> =
    upperBounds.flatMap { it.getAllReferencedClasses() }

private fun ReceiverParameterDescriptor.getAllFlowArgumentClasses(): List<ClassDescriptor> =
    type.getAllFlowArgumentClasses()

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
    get() = co.touchlab.skie.phases.features.flow.SupportedFlow.from(this) != null
