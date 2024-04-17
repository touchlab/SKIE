package co.touchlab.skie.phases.other

import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.provider.descriptor.configuration
import co.touchlab.skie.kir.descriptor.getAllExposedMembers
import co.touchlab.skie.kir.irbuilder.createFunction
import co.touchlab.skie.kir.irbuilder.util.createValueParameter
import co.touchlab.skie.kir.type.SupportedFlow
import co.touchlab.skie.kir.type.translation.from
import co.touchlab.skie.phases.ClassExportCompilerPhase
import co.touchlab.skie.phases.ClassExportPhase
import co.touchlab.skie.phases.util.StatefulCompilerDependentKirPhase
import co.touchlab.skie.phases.util.StatefulSirPhase
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.sir.element.SirVisibility
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
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.replaceArgumentsWithStarProjections

class ExtraClassExportPhase(
    private val context: ClassExportCompilerPhase.Context,
) : ClassExportCompilerPhase {

    private val descriptorProvider = context.descriptorProvider
    private val mapper = context.mapper

    context(ClassExportCompilerPhase.Context)
    override suspend fun execute() {
        val previouslyVisitedClasses = mutableSetOf<ClassDescriptor>()

        var iteration = 0

        do {
            val currentlyExportedClasses = descriptorProvider.exposedClasses

            val classesForExport = getClassesForExport(previouslyVisitedClasses)

            previouslyVisitedClasses.addAll(currentlyExportedClasses)

            val newlyDiscoveredClasses = classesForExport - currentlyExportedClasses

            exportClasses(newlyDiscoveredClasses, iteration)

            iteration++
        } while (newlyDiscoveredClasses.isNotEmpty())
    }

    context(ClassExportCompilerPhase.Context)
    private fun getClassesForExport(previouslyVisitedClasses: Set<ClassDescriptor>): Set<ClassDescriptor> {
        val result = getClassesForExportFromFlowArguments(previouslyVisitedClasses) +
            getClassesForExportFromSealedHierarchies(previouslyVisitedClasses)

        return result.toSet()
    }

    context(ClassExportPhase.Context)
    private fun getClassesForExportFromFlowArguments(previouslyVisitedClasses: Set<ClassDescriptor>): List<ClassDescriptor> {
        if (SkieConfigurationFlag.Feature_CoroutinesInterop.isDisabled) {
            return emptyList()
        }

        val allFlowArguments = getFlowArgumentsFromTopLevelMembers(previouslyVisitedClasses) + getFlowArgumentsFromNewClasses(previouslyVisitedClasses)

        return allFlowArguments.distinct().filter { mapper.shouldBeExposed(it) }
    }

    private fun getFlowArgumentsFromTopLevelMembers(previouslyVisitedClasses: Set<ClassDescriptor>): List<ClassDescriptor> =
        if (previouslyVisitedClasses.isEmpty()) {
            descriptorProvider.exposedTopLevelMembers.flatMap { it.getAllFlowArgumentClasses() }
        } else {
            // Extra exported classes do not add new top level members, so we can skip this step.
            emptyList()
        }

    private fun getFlowArgumentsFromNewClasses(previouslyVisitedClasses: Set<ClassDescriptor>): List<ClassDescriptor> {
        val newClasses = descriptorProvider.exposedClasses - previouslyVisitedClasses

        return newClasses.flatMap { it.getAllFlowArgumentClasses() }
    }

    private fun ClassDescriptor.getAllFlowArgumentClasses(): List<ClassDescriptor> =
        declaredTypeParameters.flatMap { it.getAllFlowArgumentClasses() } +
            descriptorProvider.getAllExposedMembers(this).flatMap { it.getAllFlowArgumentClasses() }

    context(ClassExportCompilerPhase.Context)
    private fun getClassesForExportFromSealedHierarchies(previouslyVisitedClasses: Set<ClassDescriptor>): List<ClassDescriptor> {
        val newClasses = descriptorProvider.exposedClasses - previouslyVisitedClasses

        return newClasses.flatMap { it.getAllExportedSealedChildren() }
    }

    context(ClassExportCompilerPhase.Context)
    private fun ClassDescriptor.getAllExportedSealedChildren(): List<ClassDescriptor> {
        if (!this.configuration[SealedInterop.ExportEntireHierarchy]) {
            return emptyList()
        }

        val topLevelSealedChildren = sealedSubclasses.filter { mapper.shouldBeExposed(it) }

        return topLevelSealedChildren + topLevelSealedChildren.getAllExportedSealedChildren()
    }

    context(ClassExportCompilerPhase.Context)
    private fun Collection<ClassDescriptor>.getAllExportedSealedChildren(): List<ClassDescriptor> =
        this.flatMap { it.getAllExportedSealedChildren() }

    context(ClassExportCompilerPhase.Context)
    private fun exportClasses(classes: Set<ClassDescriptor>, iteration: Int) {
        if (classes.isEmpty()) {
            return
        }

        val sortedClasses = classes.sortedBy { it.fqNameUnsafe.asString() }

        val stubFunction = generateStubFunction(sortedClasses, iteration)

        stubFunction.removeFromSwift()

        stubFunction.configuration.useDefaultsForSkieRuntime = true
    }

    private fun generateStubFunction(exportedClasses: Collection<ClassDescriptor>, iteration: Int): FunctionDescriptor =
        context.declarationBuilder.createFunction(
            name = "skieTypeExports_$iteration",
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
        context.doInPhase(HideExportFunctionsInitPhase) {
            val kirFunction = descriptorKirProvider.getFunction(this@removeFromSwift)

            doInPhase(HideExportFunctionsFinalizePhase) {
                kirFunction.originalSirFunction.visibility = SirVisibility.Private
            }
        }
    }

    object HideExportFunctionsInitPhase : StatefulCompilerDependentKirPhase()

    object HideExportFunctionsFinalizePhase : StatefulSirPhase()
}

private fun CallableMemberDescriptor.getAllFlowArgumentClasses(): List<ClassDescriptor> =
    typeParameters.flatMap { it.getAllFlowArgumentClasses() } +
        valueParameters.flatMap { it.type.getAllFlowArgumentClasses() } +
        (returnType?.getAllFlowArgumentClasses() ?: emptyList()) +
        (contextReceiverParameters + listOfNotNull(
            dispatchReceiverParameter,
            extensionReceiverParameter,
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
    get() = SupportedFlow.from(this) != null
