package co.touchlab.skie.configuration.provider.descriptor

import co.touchlab.skie.configuration.ClassConfiguration
import co.touchlab.skie.configuration.ConstructorConfiguration
import co.touchlab.skie.configuration.FunctionConfiguration
import co.touchlab.skie.configuration.ModuleConfiguration
import co.touchlab.skie.configuration.PropertyConfiguration
import co.touchlab.skie.configuration.SimpleFunctionConfiguration
import co.touchlab.skie.configuration.ValueParameterConfiguration
import co.touchlab.skie.configuration.provider.ConfigurationProvider
import co.touchlab.skie.configuration.provider.IdentifiedConfigurationTarget
import co.touchlab.skie.phases.ForegroundPhase
import co.touchlab.skie.phases.descriptorConfigurationProvider
import co.touchlab.skie.shim.findPackage
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.load.kotlin.toSourceElement
import org.jetbrains.kotlin.resolve.descriptorUtil.module

class DescriptorConfigurationProvider(
    private val configurationProvider: ConfigurationProvider,
) {

    private val targetCache = mutableMapOf<Any, IdentifiedConfigurationTarget>()

    fun getConfiguration(moduleDescriptor: ModuleDescriptor): ModuleConfiguration {
        val target = moduleDescriptor.getTarget()

        return configurationProvider.getConfiguration(target)
    }

    fun getConfiguration(classDescriptor: ClassDescriptor): ClassConfiguration {
        val target = classDescriptor.getTarget()

        return configurationProvider.getConfiguration(target)
    }

    fun getConfiguration(constructorDescriptor: ConstructorDescriptor): ConstructorConfiguration {
        val target = constructorDescriptor.getTarget()

        return configurationProvider.getConfiguration(target)
    }

    fun getConfiguration(simpleFunctionDescriptor: SimpleFunctionDescriptor): SimpleFunctionConfiguration {
        val target = simpleFunctionDescriptor.getTarget()

        return configurationProvider.getConfiguration(target)
    }

    fun getConfiguration(propertyDescriptor: PropertyDescriptor): PropertyConfiguration {
        val target = propertyDescriptor.getTarget()

        return configurationProvider.getConfiguration(target)
    }

    fun getConfiguration(parameterDescriptor: ParameterDescriptor): ValueParameterConfiguration {
        val target = parameterDescriptor.getTarget()

        return configurationProvider.getConfiguration(target)
    }

    private fun ModuleDescriptor.getTarget(): IdentifiedConfigurationTarget.Module =
        targetCache.getOrPut(this.original) {
            DescriptorBasedConfigurationTarget.Module(this)
        } as IdentifiedConfigurationTarget.Module

    private fun PackageFragmentDescriptor.getTarget(): IdentifiedConfigurationTarget.Package =
        targetCache.getOrPut(this.original) {
            DescriptorBasedConfigurationTarget.Package(module.getTarget(), this)
        } as IdentifiedConfigurationTarget.Package

    private fun SourceFile.getTarget(packageFragmentDescriptor: PackageFragmentDescriptor): IdentifiedConfigurationTarget.File =
        targetCache.getOrPut(this to packageFragmentDescriptor.original) {
            DescriptorBasedConfigurationTarget.File(packageFragmentDescriptor.getTarget())
        } as IdentifiedConfigurationTarget.File

    private fun ClassDescriptor.getTarget(): IdentifiedConfigurationTarget.Class =
        targetCache.getOrPut(this.original) {
            val parent = getFileOrClassParent()

            DescriptorBasedConfigurationTarget.Class(parent, this)
        } as IdentifiedConfigurationTarget.Class

    private fun ConstructorDescriptor.getTarget(): IdentifiedConfigurationTarget.Constructor =
        targetCache.getOrPut(this.original) {
            val parent = getFileOrClassParent()

            DescriptorBasedConfigurationTarget.Constructor(parent, this)
        } as IdentifiedConfigurationTarget.Constructor

    private fun SimpleFunctionDescriptor.getTarget(): IdentifiedConfigurationTarget.SimpleFunction =
        targetCache.getOrPut(this.original) {
            val parent = getFileOrClassParent()

            DescriptorBasedConfigurationTarget.SimpleFunction(parent, this)
        } as IdentifiedConfigurationTarget.SimpleFunction

    private fun PropertyDescriptor.getTarget(): IdentifiedConfigurationTarget.Property =
        targetCache.getOrPut(this.original) {
            val parent = getFileOrClassParent()

            DescriptorBasedConfigurationTarget.Property(parent, this)
        } as IdentifiedConfigurationTarget.Property

    private fun ParameterDescriptor.getTarget(): IdentifiedConfigurationTarget.ValueParameter =
        targetCache.getOrPut(this.original) {
            val parent = getCallableDeclarationParent()

            DescriptorBasedConfigurationTarget.ValueParameter(parent, this)
        } as IdentifiedConfigurationTarget.ValueParameter

    private fun DeclarationDescriptor.getFileOrClassParent(): IdentifiedConfigurationTarget.FileOrClass {
        val containingDeclaration = containingDeclaration

        return if (containingDeclaration is ClassDescriptor) {
            containingDeclaration.getTarget()
        } else {
            toSourceElement.containingFile.getTarget(findPackage())
        }
    }

    private fun ParameterDescriptor.getCallableDeclarationParent(): IdentifiedConfigurationTarget.ValueParameterParent =
        when (val containingDeclaration = containingDeclaration) {
            is SimpleFunctionDescriptor -> containingDeclaration.getTarget()
            is ConstructorDescriptor -> containingDeclaration.getTarget()
            is PropertyDescriptor -> containingDeclaration.getTarget()
            is PropertyAccessorDescriptor -> containingDeclaration.correspondingProperty.getTarget()
            is ClassDescriptor -> containingDeclaration.getTarget()
            else -> error("Unsupported function parent: $containingDeclaration")
        }
}

context(ForegroundPhase.Context)
val ClassDescriptor.configuration: ClassConfiguration
    get() = descriptorConfigurationProvider.getConfiguration(this)

context(ForegroundPhase.Context)
val SimpleFunctionDescriptor.configuration: SimpleFunctionConfiguration
    get() = descriptorConfigurationProvider.getConfiguration(this)

context(ForegroundPhase.Context)
val ConstructorDescriptor.configuration: ConstructorConfiguration
    get() = descriptorConfigurationProvider.getConfiguration(this)

context(ForegroundPhase.Context)
val FunctionDescriptor.configuration: FunctionConfiguration
    get() = when (this) {
        is SimpleFunctionDescriptor -> configuration
        is ConstructorDescriptor -> configuration
        else -> error("Unsupported function descriptor: $this")
    }
