package co.touchlab.skie.plugin.generator.internal.util.irbuilder

import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

internal interface DeclarationBuilder {

    fun getCustomNamespace(name: String): Namespace<PackageFragmentDescriptor>

    fun getClassNamespace(classDescriptor: ClassDescriptor): Namespace<ClassDescriptor>

    fun getPackageNamespace(existingMember: FunctionDescriptor): Namespace<PackageFragmentDescriptor>?

    fun getPackageNamespaceOrCustom(existingMember: FunctionDescriptor): Namespace<PackageFragmentDescriptor>

    fun createFunction(
        name: Name,
        namespace: Namespace<*>,
        annotations: Annotations,
        builder: FunctionBuilder.() -> Unit,
    ): FunctionDescriptor

    fun createSecondaryConstructor(
        name: Name = SpecialNames.INIT,
        namespace: Namespace<ClassDescriptor>,
        annotations: Annotations,
        builder: SecondaryConstructorBuilder.() -> Unit,
    ): ClassConstructorDescriptor
}

internal fun DeclarationBuilder.getNamespace(function: FunctionDescriptor): Namespace<*> =
    when (val containingDeclaration = function.containingDeclaration) {
        is ClassDescriptor -> getClassNamespace(containingDeclaration)
        is PackageFragmentDescriptor -> getPackageNamespaceOrCustom(function)
        else -> throw UnsupportedDeclarationDescriptorException(containingDeclaration)
    }

internal fun DeclarationBuilder.getNamespace(constructor: ClassConstructorDescriptor): Namespace<ClassDescriptor> =
    getClassNamespace(constructor.containingDeclaration)

internal fun DeclarationBuilder.createFunction(
    name: String,
    namespace: Namespace<*>,
    annotations: Annotations,
    builder: FunctionBuilder.() -> Unit,
): FunctionDescriptor = createFunction(Name.identifier(name), namespace, annotations, builder)

internal fun DeclarationBuilder.createSecondaryConstructor(
    name: String = SpecialNames.INIT.asString(),
    namespace: Namespace<ClassDescriptor>,
    annotations: Annotations,
    builder: SecondaryConstructorBuilder.() -> Unit,
): ClassConstructorDescriptor = createSecondaryConstructor(Name.special(name), namespace, annotations, builder)
