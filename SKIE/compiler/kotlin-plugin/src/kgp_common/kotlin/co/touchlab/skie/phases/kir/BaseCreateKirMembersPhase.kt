package co.touchlab.skie.phases.kir

import co.touchlab.skie.kir.element.DeprecationLevel
import co.touchlab.skie.kir.element.KirCallableDeclaration.Origin
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirScope
import co.touchlab.skie.phases.DescriptorConversionPhase
import org.jetbrains.kotlin.backend.konan.KonanFqNames
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerDesc
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.resolve.deprecation.DeprecationLevelValue
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass

abstract class BaseCreateKirMembersPhase(
    context: DescriptorConversionPhase.Context,
    private val supportsConstructors: Boolean = false,
    private val supportsSimpleFunctions: Boolean = false,
    private val supportsProperties: Boolean = false,
) : DescriptorConversionPhase {

    protected val descriptorProvider = context.descriptorProvider
    protected val mapper = context.mapper
    protected val kirProvider = context.kirProvider
    protected val descriptorKirProvider = context.descriptorKirProvider
    protected val descriptorConfigurationProvider = context.descriptorConfigurationProvider
    protected val kirDeclarationTypeTranslator = context.kirDeclarationTypeTranslator
    protected val namer = context.namer

    context(DescriptorConversionPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinClasses.forEach(::createMembers)
    }

    private fun createMembers(kirClass: KirClass) {
        if (kirClass in kirProvider.kirBuiltins.builtinClasses) {
            // TODO Implement accurate way to generate builtin members once needed
            return
        }

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
            is SimpleFunctionDescriptor -> createSimpleFunctionIfSupported(descriptor, kirClass, origin)
            is PropertyDescriptor -> {
                if (mapper.isObjCProperty(descriptor.baseProperty)) {
                    createPropertyIfSupported(descriptor, kirClass, origin)
                } else {
                    descriptor.getter?.let { createSimpleFunctionIfSupported(it, kirClass, origin) }
                    descriptor.setter?.let { createSimpleFunctionIfSupported(it, kirClass, origin) }
                }
            }
            else -> error("Unsupported member: $descriptor")
        }
    }

    private fun createSimpleFunctionIfSupported(
        descriptor: FunctionDescriptor,
        kirClass: KirClass,
        origin: Origin,
    ) {
        if (supportsSimpleFunctions) {
            visitSimpleFunction(descriptor, kirClass, origin)
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

    protected open fun visitSimpleFunction(
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

    protected val PropertyDescriptor.baseProperty: PropertyDescriptor
        get() = (getAllParents(this) + this.original).first { mapper.isBaseProperty(it) }

    private fun getAllParents(descriptor: PropertyDescriptor): List<PropertyDescriptor> =
        getDirectParents(descriptor).flatMap { getAllParents(it) + it.original }

    protected fun getDirectParents(descriptor: PropertyDescriptor): List<PropertyDescriptor> =
        descriptor.overriddenDescriptors.map { it.original }
            .filter { mapper.shouldBeExposed(it) }

    protected val CallableMemberDescriptor.kirDeprecationLevel: DeprecationLevel
        get() {
            val deprecationInfo = mapper.getDeprecation(this)

            return when (deprecationInfo?.deprecationLevel) {
                DeprecationLevelValue.ERROR -> DeprecationLevel.Error(deprecationInfo.message)
                DeprecationLevelValue.WARNING -> DeprecationLevel.Warning(deprecationInfo.message)
                DeprecationLevelValue.HIDDEN -> DeprecationLevel.Error(deprecationInfo.message)
                null -> DeprecationLevel.None
            }
        }

    protected val CallableMemberDescriptor.signature: String
        get() = with(KonanManglerDesc) {
            this@signature.signatureString(false)
        }

    protected val KirClass.callableDeclarationScope: KirScope
        get() = when (this.kind) {
            KirClass.Kind.File -> KirScope.Static
            else -> KirScope.Member
        }

    protected val CallableMemberDescriptor.isRefinedInSwift: Boolean
        get() = annotations.any { annotation ->
            annotation.annotationClass?.annotations?.any { it.fqName == KonanFqNames.refinesInSwift } == true
        }
}
