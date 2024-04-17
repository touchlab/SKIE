package co.touchlab.skie.phases.kir

import co.touchlab.skie.kir.element.KirCallableDeclaration.Origin
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.CompilerDependentDescriptorConversionPhase
import co.touchlab.skie.phases.DescriptorConversionPhase
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

internal abstract class BaseCreateRegularKirMembersPhase(
    context: CompilerDependentDescriptorConversionPhase.Context,
    private val supportsConstructors: Boolean = false,
    private val supportsSimpleFunctions: Boolean = false,
    private val supportsProperties: Boolean = false,
) : BaseCreateKirMembersPhase(context) {

    context(CompilerDependentDescriptorConversionPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinClasses.forEach(::createMembers)
    }

    private fun createMembers(kirClass: KirClass) {
        when (kirClass.kind) {
            KirClass.Kind.File -> createMembers(descriptorKirProvider.getClassSourceFile(kirClass), kirClass)
            else -> createMembers(descriptorKirProvider.getClassDescriptor(kirClass), kirClass)
        }
    }

    private fun createMembers(sourceFile: SourceFile, kirClass: KirClass) {
        descriptorProvider.getExposedStaticMembers(sourceFile).forEach {
            val scope = if (it.extensionReceiverParameter != null) Origin.Extension else Origin.Global

            createMember(it, kirClass, scope)
        }
    }

    private fun createMembers(classDescriptor: ClassDescriptor, kirClass: KirClass) {
        if (supportsConstructors) {
            descriptorProvider.getExposedConstructors(classDescriptor).forEach {
                visitConstructor(it, kirClass)
            }
        }

        if (supportsSimpleFunctions || supportsProperties) {
            descriptorProvider.getExposedClassMembers(classDescriptor).forEach {
                createMember(it, kirClass, Origin.Member)
            }

            descriptorProvider.getExposedCategoryMembers(classDescriptor).forEach {
                createMember(it, kirClass, Origin.Extension)
            }
        }
    }

    private fun createMember(descriptor: CallableMemberDescriptor, kirClass: KirClass, origin: Origin) {
        when (descriptor) {
            is SimpleFunctionDescriptor -> createFunctionIfSupported(descriptor, kirClass, origin)
            is PropertyDescriptor -> {
                if (mapper.isObjCProperty(descriptor.baseProperty)) {
                    createPropertyIfSupported(descriptor, kirClass, origin)
                } else {
                    descriptor.getter?.let { createFunctionIfSupported(it, kirClass, origin) }
                    descriptor.setter?.let { createFunctionIfSupported(it, kirClass, origin) }
                }
            }
            else -> error("Unsupported member: $descriptor")
        }
    }

    private fun createFunctionIfSupported(
        descriptor: FunctionDescriptor,
        kirClass: KirClass,
        origin: Origin,
    ) {
        if (supportsSimpleFunctions) {
            visitFunction(descriptor, kirClass, origin)
        }
    }

    private fun createPropertyIfSupported(
        descriptor: PropertyDescriptor,
        kirClass: KirClass,
        origin: Origin,
    ) {
        if (supportsProperties) {
            visitProperty(descriptor, kirClass, origin)
        }
    }

    protected open fun visitConstructor(
        descriptor: ConstructorDescriptor,
        kirClass: KirClass,
    ) {
        error("Constructors are not supported.")
    }

    protected open fun visitFunction(
        descriptor: FunctionDescriptor,
        kirClass: KirClass,
        origin: Origin,
    ) {
        error("Functions are not supported.")
    }

    protected open fun visitProperty(
        descriptor: PropertyDescriptor,
        kirClass: KirClass,
        origin: Origin,
    ) {
        error("Properties are not supported.")
    }
}
