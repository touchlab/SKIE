@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.swiftlink.plugin

import co.touchlab.swiftpack.api.DefaultMutableSwiftFunctionName
import co.touchlab.swiftpack.api.DefaultMutableSwiftParameterName
import co.touchlab.swiftpack.api.DefaultMutableSwiftTypeName
import co.touchlab.swiftpack.api.MutableSwiftFunctionName
import co.touchlab.swiftpack.api.MutableSwiftTypeName
import co.touchlab.swiftpack.api.SwiftBridgedName
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.getClassIfCategory
import co.touchlab.swiftlink.plugin.reflection.reflectors.mapper
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

internal fun String.splitByLast(separator: String): Pair<String, String> {
    val lastSeparatorIndex = lastIndexOf(separator)
    return if (lastSeparatorIndex == -1) {
        "" to this
    } else {
        substring(0, lastSeparatorIndex) to substring(lastSeparatorIndex + 1)
    }
}

internal class TransformAccumulator(
    private val namer: ObjCExportNamer,
) {
    val typeNames = mutableMapOf<TypeTransformTarget, MutableSwiftTypeName>()
    val functionNames = mutableMapOf<FunctionDescriptor, MutableSwiftFunctionName>()

    private val mutableTypeTransforms = mutableMapOf<TypeTransformTarget, ObjcClassTransformScope>()
    val typeTransforms: Map<TypeTransformTarget, ObjcClassTransformScope> = mutableTypeTransforms

    operator fun get(descriptor: ClassDescriptor): ObjcClassTransformScope? = mutableTypeTransforms[TypeTransformTarget.Class(descriptor)]

    fun transform(descriptor: ClassDescriptor): ObjcClassTransformScope {
        val target = TypeTransformTarget.Class(descriptor)

        return mutableTypeTransforms.getOrPut(target) {
            ObjcClassTransformScope(resolveName(target))
        }
    }

    operator fun get(descriptor: PropertyDescriptor): ObjcPropertyTransformScope? = mutableTypeTransforms[descriptor.containingTarget]?.properties?.get(descriptor)

    fun transform(descriptor: PropertyDescriptor): ObjcPropertyTransformScope = typeTransform(descriptor.containingTarget).properties.getOrPut(descriptor) {
        ObjcPropertyTransformScope()
    }

    operator fun get(descriptor: FunctionDescriptor): ObjcMethodTransformScope? = mutableTypeTransforms[descriptor.containingTarget]?.methods?.get(descriptor)

    fun transform(descriptor: FunctionDescriptor): ObjcMethodTransformScope = typeTransform(descriptor.containingTarget).methods.getOrPut(descriptor) {
        ObjcMethodTransformScope(swiftName = resolveName(descriptor))
    }

    fun resolveName(target: TypeTransformTarget): MutableSwiftTypeName = typeNames.getOrPut(target) {
        when (target) {
            is TypeTransformTarget.Class -> {
                val name = if (target.descriptor.kind == ClassKind.ENUM_ENTRY) {
                    namer.getEnumEntrySelector(target.descriptor)
                } else {
                    namer.getClassOrProtocolName(target.descriptor).swiftName
                }
                when (val parent = target.descriptor.containingDeclaration) {
                    is PackageFragmentDescriptor, is PackageViewDescriptor -> DefaultMutableSwiftTypeName(
                        originalParent = null,
                        originalIsNestedInParent = false,
                        originalSimpleName = name,
                    )
                    is ClassDescriptor -> {
                        val parentName = resolveName(TypeTransformTarget.Class(parent))
                        val parentQualifiedName = parentName.originalQualifiedName
                        val simpleNameCandidate = if (name.startsWith(parentQualifiedName)) {
                            name.drop(parentQualifiedName.length)
                        } else {
                            name
                        }
                        val (isNestedInParent, simpleName) = if (simpleNameCandidate.startsWith('.')) {
                            true to simpleNameCandidate.drop(1)
                        } else {
                            false to simpleNameCandidate
                        }
                        DefaultMutableSwiftTypeName(
                            originalParent = parentName,
                            originalIsNestedInParent = isNestedInParent,
                            originalSimpleName = simpleName,
                        )
                    }
                    else -> error("Unexpected parent type: $parent")
                }
            }
            is TypeTransformTarget.File -> {
                DefaultMutableSwiftTypeName(
                    originalParent = null,
                    originalIsNestedInParent = false,
                    originalSimpleName = namer.getFileClassName(target.file).swiftName,
                )
            }
        }
    }

    fun resolveName(functionDescriptor: FunctionDescriptor): MutableSwiftFunctionName = functionNames.getOrPut(functionDescriptor) {
        val swiftName = namer.getSwiftName(functionDescriptor)
        val (functionName, parameters) = swiftName.splitByLast("(").let { (name, parameters) ->
            name to parameters.dropLast(1).split(":").map { it.trim() }.filter { it.isNotEmpty() }
        }

        // Parameters seem duplicite, but we need separate instances for renaming.
        DefaultMutableSwiftFunctionName(
            functionName,
            parameters.map { DefaultMutableSwiftParameterName(it) },
            functionName,
            parameters.map { DefaultMutableSwiftParameterName(it) },
        )
    }

    fun close() {
        // TODO: Add check for closed state in all mutating methods.
        ensureTypesRenamedWhereNeeded()
        ensureChildClassesRenamedWhereNeeded()
        ensureFunctionsRenamedWhereNeeded()
    }

    private fun ensureTypesRenamedWhereNeeded() {
        typeNames.forEach { (target, name) ->
            if (name.isChanged) {
                typeTransform(target)
            }
        }
    }

    private fun ensureChildClassesRenamedWhereNeeded() {
        fun touchNestedClassTransforms(descriptor: ClassDescriptor) {
            return descriptor.unsubstitutedMemberScope.getContributedDescriptors().filterIsInstance<ClassDescriptor>()
                .filter { it.kind == ClassKind.CLASS || it.kind == ClassKind.OBJECT }
                .forEach { childDescriptor ->
                    val target = TypeTransformTarget.Class(childDescriptor)
                    val transform = typeTransform(target)
                    assert(transform.newSwiftName != null) { "Expected to have a new name for $childDescriptor" }

                    touchNestedClassTransforms(childDescriptor)
                }
        }

        mutableTypeTransforms
            .toList()
            .forEach { (target, transform) ->
                if (target is TypeTransformTarget.Class && transform.newSwiftName != null) {
                    touchNestedClassTransforms(target.descriptor)
                }
            }
    }

    private fun ensureFunctionsRenamedWhereNeeded() {
        functionNames.forEach { (descriptor, name) ->
            if (name.isChanged) {
                transform(descriptor)
            }
        }
    }

    private val CallableMemberDescriptor.containingTarget: TypeTransformTarget
        get() {
            val categoryClass = namer.mapper.getClassIfCategory(this)
            if (categoryClass != null) {
                return TypeTransformTarget.Class(categoryClass)
            }
            return when (val containingDeclaration = containingDeclaration) {
                is ClassDescriptor -> TypeTransformTarget.Class(containingDeclaration)
                is PackageFragmentDescriptor -> TypeTransformTarget.File(findSourceFile())
                else -> error("Unexpected containing declaration: $containingDeclaration")
            }
        }

    private fun typeTransform(typeTransformTarget: TypeTransformTarget): ObjcClassTransformScope {
        return mutableTypeTransforms.getOrPut(typeTransformTarget) {
            ObjcClassTransformScope(resolveName(typeTransformTarget))
        }
    }

    class ObjcClassTransformScope(
        var swiftName: MutableSwiftTypeName,
        var isRemoved: Boolean = false,
        var isHidden: Boolean = false,
        var bridge: SwiftBridgedName? = null,
    ) {
        val newSwiftName: MutableSwiftTypeName?
            get() = swiftName.takeIf { it.isChanged }

        val properties = mutableMapOf<PropertyDescriptor, ObjcPropertyTransformScope>()
        val methods = mutableMapOf<FunctionDescriptor, ObjcMethodTransformScope>()
    }

    class ObjcPropertyTransformScope(
        var isRemoved: Boolean = false,
        var isHidden: Boolean = false,
        var rename: String? = null,
    )

    class ObjcMethodTransformScope(
        var isRemoved: Boolean = false,
        var isHidden: Boolean = false,
        var swiftName: MutableSwiftFunctionName,
    ) {
        val newSwiftName: MutableSwiftFunctionName?
            get() = swiftName.takeIf { it.isChanged }
    }

    sealed interface TypeTransformTarget {
        data class Class(val descriptor: ClassDescriptor) : TypeTransformTarget
        data class File(val file: SourceFile): TypeTransformTarget
    }
}
