package co.touchlab.swiftgen.plugin.internal.util.ir

import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

internal interface DeclarationBuilder {

    fun getNamespace(name: String): Namespace<PackageFragmentDescriptor>

    fun getNamespace(classDescriptor: ClassDescriptor): Namespace<ClassDescriptor>

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
