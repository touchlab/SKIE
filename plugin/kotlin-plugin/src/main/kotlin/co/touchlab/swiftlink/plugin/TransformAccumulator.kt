package co.touchlab.swiftlink.plugin

import co.touchlab.swiftpack.api.DefaultMutableSwiftTypeName
import co.touchlab.swiftpack.api.MutableSwiftTypeName
import co.touchlab.swiftpack.api.SwiftBridgedName
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

internal class TransformAccumulator(
    private val namer: ObjCExportNamer,
) {
    val typeNames = mutableMapOf<TypeTransformTarget, MutableSwiftTypeName>()

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
        ObjcMethodTransformScope()
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
                        originalSeparator = "",
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
                        val (separator, simpleName) = if (simpleNameCandidate.startsWith('.')) {
                            "." to simpleNameCandidate.drop(1)
                        } else {
                            "" to simpleNameCandidate
                        }
                        DefaultMutableSwiftTypeName(
                            originalParent = parentName,
                            originalSeparator = separator,
                            originalSimpleName = simpleName,
                        )
                    }
                    else -> error("Unexpected parent type: $parent")
                }
            }
            is TypeTransformTarget.File -> {
                DefaultMutableSwiftTypeName(
                    originalParent = null,
                    originalSeparator = "",
                    originalSimpleName = namer.getFileClassName(target.file).swiftName,
                )
            }
        }

    }

    fun ensureChildClassesRenamedWhereNeeded() {
        fun touchNestedClassTransforms(descriptor: ClassDescriptor) {
            return descriptor.unsubstitutedMemberScope.getContributedDescriptors().filterIsInstance<ClassDescriptor>()
                .filter { it.kind == ClassKind.CLASS || it.kind == ClassKind.OBJECT }
                .forEach { childDescriptor ->
                    val target = TransformAccumulator.TypeTransformTarget.Class(childDescriptor)
                    val transform = typeTransform(target)
                    assert(transform.newSwiftName != null) { "Expected to have a new name for $childDescriptor" }

                    touchNestedClassTransforms(childDescriptor)
                }
        }

        mutableTypeTransforms
            .forEach { (target, transform) ->
                if (target is TransformAccumulator.TypeTransformTarget.Class && transform.newSwiftName != null) {
                    touchNestedClassTransforms(target.descriptor)
                }
            }
    }

    private val CallableMemberDescriptor.containingTarget: TypeTransformTarget
        get() = when (val containingDeclaration = containingDeclaration) {
            is ClassDescriptor -> TypeTransformTarget.Class(containingDeclaration)
            is PackageFragmentDescriptor -> TypeTransformTarget.File(findSourceFile())
            else -> error("Unexpected containing declaration: $containingDeclaration")
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
        var rename: String? = null,
    )

    sealed interface TypeTransformTarget {
        data class Class(val descriptor: ClassDescriptor) : TypeTransformTarget
        data class File(val file: SourceFile): TypeTransformTarget
    }
}
