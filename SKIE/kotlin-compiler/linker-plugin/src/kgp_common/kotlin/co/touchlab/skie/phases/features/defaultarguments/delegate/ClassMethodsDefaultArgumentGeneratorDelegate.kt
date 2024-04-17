package co.touchlab.skie.phases.features.defaultarguments.delegate

import co.touchlab.skie.kir.descriptor.DescriptorProvider
import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.util.SharedCounter
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

class ClassMethodsDefaultArgumentGeneratorDelegate(
    context: DescriptorModificationPhase.Context,
    sharedCounter: SharedCounter,
) : BaseFunctionDefaultArgumentGeneratorDelegate(context, sharedCounter) {

    override fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor> =
        this.allSupportedClasses().flatMap { classDescriptor ->
            classDescriptor.allSupportedMethods()
        }

    private fun DescriptorProvider.allSupportedClasses(): List<ClassDescriptor> =
        this.exposedClasses.filter { it.isSupported }

    private val ClassDescriptor.isSupported: Boolean
        get() = when (this.kind) {
            ClassKind.CLASS, ClassKind.ENUM_CLASS, ClassKind.OBJECT -> true
            ClassKind.INTERFACE, ClassKind.ENUM_ENTRY, ClassKind.ANNOTATION_CLASS -> false
        }

    private fun ClassDescriptor.allSupportedMethods(): List<SimpleFunctionDescriptor> =
        descriptorProvider.getExposedClassMembers(this)
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { it.isSupported }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.contextReceiverParameters.isEmpty()
}
