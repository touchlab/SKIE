package co.touchlab.skie.configuration.provider.descriptor

import co.touchlab.skie.configuration.provider.IdentifiedConfigurationTarget
import co.touchlab.skie.kir.util.findAnnotation
import co.touchlab.skie.kir.util.hasAnnotation
import co.touchlab.skie.phases.runtime.belongsToSkieKotlinRuntime
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import kotlin.reflect.KClass

object DescriptorBasedConfigurationTarget {

    abstract class Declaration(
        private val declarationDescriptor: DeclarationDescriptor,
    ) : IdentifiedConfigurationTarget {

        override val belongsToSkieRuntime: Boolean = declarationDescriptor.belongsToSkieKotlinRuntime

        override val fqName: String = declarationDescriptor.fqNameSafe.asString()

        override fun <T : Annotation> hasAnnotation(kClass: KClass<T>): Boolean =
            declarationDescriptor.hasAnnotation(kClass)

        override fun <T : Annotation> findAnnotation(kClass: KClass<T>): T? =
            declarationDescriptor.findAnnotation(kClass)
    }

    class Module(
        moduleDescriptor: ModuleDescriptor,
    ) : Declaration(moduleDescriptor), IdentifiedConfigurationTarget.Module

    class Package(
        override val parent: IdentifiedConfigurationTarget.Module,
        packageFragmentDescriptor: PackageFragmentDescriptor,
    ) : Declaration(packageFragmentDescriptor), IdentifiedConfigurationTarget.Package

    class File(
        override val parent: IdentifiedConfigurationTarget.Package,
    ) : IdentifiedConfigurationTarget.File {

        override val fqName: String = ""

        // Currently not needed to be implemented because the configuration is not used for files directly and in the case of runtime is not inherited.
        override val belongsToSkieRuntime: Boolean = false

        override fun <T : Annotation> hasAnnotation(kClass: KClass<T>): Boolean = false

        override fun <T : Annotation> findAnnotation(kClass: KClass<T>): T? = null
    }

    class Class(
        override val parent: IdentifiedConfigurationTarget.FileOrClass,
        classDescriptor: ClassDescriptor,
    ) : Declaration(classDescriptor), IdentifiedConfigurationTarget.Class

    class Constructor(
        override val parent: IdentifiedConfigurationTarget.FileOrClass,
        constructorDescriptor: ConstructorDescriptor,
    ) : Declaration(constructorDescriptor), IdentifiedConfigurationTarget.Constructor

    class SimpleFunction(
        override val parent: IdentifiedConfigurationTarget.FileOrClass,
        functionDescriptor: SimpleFunctionDescriptor,
    ) : Declaration(functionDescriptor), IdentifiedConfigurationTarget.SimpleFunction

    class Property(
        override val parent: IdentifiedConfigurationTarget.FileOrClass,
        propertyDescriptor: PropertyDescriptor,
    ) : Declaration(propertyDescriptor), IdentifiedConfigurationTarget.Property

    class ValueParameter(
        override val parent: IdentifiedConfigurationTarget.ValueParameterParent,
        valueParameterDescriptor: ParameterDescriptor,
    ) : Declaration(valueParameterDescriptor), IdentifiedConfigurationTarget.ValueParameter
}
