package co.touchlab.swiftgen.plugin.internal.util.ir

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name

internal interface DeclarationBuilder {

    fun getNamespace(name: String): Namespace

    fun getNamespace(classDescriptor: ClassDescriptor): Namespace

    fun createFunction(
        name: Name,
        namespace: Namespace,
        annotations: Annotations = Annotations.EMPTY,
        builder: FunctionBuilder.() -> Unit,
    ): FunctionDescriptor
}

internal fun DeclarationBuilder.createFunction(
    name: String,
    namespace: Namespace,
    annotations: Annotations = Annotations.EMPTY,
    builder: FunctionBuilder.() -> Unit,
): FunctionDescriptor = createFunction(Name.identifier(name), namespace, annotations, builder)
