package co.touchlab.skie.plugin.generator.internal.export

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.MutableDescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.allExposedMembers
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.util.flow.SupportedFlow
import co.touchlab.skie.plugin.generator.internal.configuration.ConfigurationContainer
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
import org.jetbrains.kotlin.types.typeUtil.replaceArgumentsWithStarProjections

internal class ExtraClassExportPhase(
    override val skieContext: SkieContext,
    private val descriptorProvider: MutableDescriptorProvider,
    private val declarationBuilder: DeclarationBuilder,
) : SkieCompilationPhase, ConfigurationContainer {

    override val isActive: Boolean = true

    override fun runClassExportingPhase() {
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
        if (SkieConfigurationFlag.CoroutinesInterop !in skieContext.skieConfiguration.enabledConfigurationFlags) {
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
        if (!this.getConfiguration(SealedInterop.ExportEntireHierarchy)) {
            return emptyList()
        }

        val topLevelSealedChildren = sealedSubclasses.filter { descriptorProvider.isExposable(it) }

        return topLevelSealedChildren + topLevelSealedChildren.getAllExportedSealedChildren()
    }

    private fun Collection<ClassDescriptor>.getAllExportedSealedChildren(): List<ClassDescriptor> =
        this.flatMap { it.getAllExportedSealedChildren() }

    private fun generateStubFunction(exportedClasses: Collection<ClassDescriptor>): FunctionDescriptor =
        declarationBuilder.createFunction(
            name = "skieTypeExports",
            namespace = declarationBuilder.getCustomNamespace("SkieTypeExports"),
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
        skieContext.module.configure {
            this.swiftModel.visibility = SwiftModelVisibility.Removed
        }
    }
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
