package co.touchlab.swiftlink.plugin.transform

import co.touchlab.swiftlink.plugin.resolve.KotlinSymbolResolver
import co.touchlab.swiftpack.spec.module.ApiTransform
import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinTypeParameterReference
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue


class ApiTransformResolver(
    private val namer: ObjCExportNamer,
    private val symbolResolver: KotlinSymbolResolver,
    private val transforms: Collection<ApiTransform>,
) {

    val resolvedTransforms: Collection<ResolvedApiTransform> by lazy {
        // TODO: "ResolvedName" is a bit of a misnomer, since it's not resolved yet here
        val typeNames = mutableMapOf<ClassDescriptor, ResolvedName>()
        fun resolveName(
            descriptor: ClassDescriptor,
            nameFactory: Lazy<ObjCExportNamer.ClassOrProtocolName> = lazy { namer.getClassOrProtocolName(descriptor) },
        ): ResolvedName = typeNames.getOrPut(descriptor) {
            val name by nameFactory
            when (val parent = descriptor.containingDeclaration) {
                is PackageFragmentDescriptor, is PackageViewDescriptor -> ResolvedName(
                    parent = null,
                    separator = "",
                    originalSimpleName = name.swiftName,
                    newSimpleName = null
                )
                is ClassDescriptor -> {
                    val parentName = resolveName(parent)
                    val parentQualifiedName = parentName.originalQualifiedName()
                    assert(name.swiftName.startsWith(parentQualifiedName))
                    val simpleNameCandidate = name.swiftName.drop(parentQualifiedName.length)
                    val (separator, simpleName) = if (simpleNameCandidate.startsWith('.')) {
                        "." to simpleNameCandidate.drop(1)
                    } else {
                        "" to simpleNameCandidate
                    }
                    ResolvedName(
                        parent = parentName,
                        separator = separator,
                        originalSimpleName = simpleName,
                        newSimpleName = null,
                    )
                }
                else -> error("Unexpected parent type: $parent")
            }
        }

        val syntheticContainerTransformCandidates = mutableMapOf<ResolvedApiTransform.Target.CallableMemberParent, Boolean>()
        val classRenameTransformTargets = mutableSetOf<ResolvedApiTransform.Target.Type>()
        val explicitTransforms = transforms.map { transform ->
            when (transform) {
                is ApiTransform.EnumEntryTransform -> TODO()
                is ApiTransform.FileTransform -> TODO()
                is ApiTransform.FunctionTransform -> {
                    val function = symbolResolver.resolveFunction(transform.functionId)
                    val selector = namer.getSelector(function)
                    val swiftName = namer.getSwiftName(function)

                    syntheticContainerTransformCandidates.putIfAbsent(function.parent, true)

                    ResolvedApiTransform.FunctionTransform(
                        target = ResolvedApiTransform.Target.Function(function),
                        selector = selector,
                        newSwiftSelector = transform.rename ?: transform.hide.ifTrue { "__${swiftName}" },
                        hide = transform.hide,
                        remove = transform.remove,
                        isStatic = function.dispatchReceiverParameter == null,
                    )
                }
                is ApiTransform.PropertyTransform -> {
                    val property = symbolResolver.resolveProperty(transform.propertyId)
                    val name = namer.getPropertyName(property)

                    syntheticContainerTransformCandidates.putIfAbsent(property.parent, true)

                    ResolvedApiTransform.PropertyTransform(
                        target = ResolvedApiTransform.Target.Property(property),
                        name = name,
                        newSwiftName = transform.rename ?: transform.hide.ifTrue { "__${name}" },
                        hide = transform.hide,
                        remove = transform.remove,
                        isStatic = property.dispatchReceiverParameter == null,
                    )
                }
                is ApiTransform.TypeTransform -> when (val typeId = transform.typeId) {
                    is KotlinClassReference.Id -> {
                        val descriptor = symbolResolver.resolveClass(typeId)
                        val target = ResolvedApiTransform.Target.Type(descriptor)
                        val name = namer.getClassOrProtocolName(descriptor)
                        val resolvedName = resolveName(descriptor, lazy { name })
                        val finalRename = transform.rename ?: transform.hide.ifTrue {
                            ApiTransform.TypeTransform.Rename(
                                kind = ApiTransform.TypeTransform.Rename.Kind.RELATIVE,
                                action = ApiTransform.TypeTransform.Rename.Action.Prefix("__"),
                            )
                        }
                        if (finalRename != null) {
                            resolvedName.apply(finalRename)
                            classRenameTransformTargets.add(target)
                        }

                        syntheticContainerTransformCandidates[target] = false

                        ResolvedApiTransform.TypeTransform(
                            target = target,
                            isProtocol = descriptor.kind == ClassKind.INTERFACE,
                            classOrProtocolName = name,
                            newSwiftName = resolvedName,
                            bridgedName = when (val bridge = transform.bridge) {
                                is ApiTransform.TypeTransform.Bridge.Absolute -> BridgedName.Absolute(bridge.swiftType)
                                is ApiTransform.TypeTransform.Bridge.Relative -> BridgedName.Relative(
                                    resolveName(symbolResolver.resolveClass(bridge.parentKotlinClass)),
                                    bridge.swiftType,
                                )

                                null -> null
                            },
                            hide = transform.hide,
                            remove = transform.remove,
                        )
                    }
                    is KotlinTypeParameterReference.Id -> TODO()
                }
            }
        }

        val containerTransforms = syntheticContainerTransformCandidates.filterValues { it }.map { (parent, _) ->
            when (parent) {
                is ResolvedApiTransform.Target.Type -> ResolvedApiTransform.TypeTransform(
                    target = parent,
                    hide = false,
                    remove = false,
                    isProtocol = parent.descriptor.kind == ClassKind.INTERFACE,
                    classOrProtocolName = namer.getClassOrProtocolName(parent.descriptor),
                    newSwiftName = null,
                    bridgedName = null,
                )
                is ResolvedApiTransform.Target.File -> ResolvedApiTransform.FileTransform(
                    target = parent,
                    classOrProtocolName = namer.getFileClassName(parent.file),
                    hide = false,
                    remove = false,
                    newSwiftName = null,
                    bridgedName = null,
                )
            }
        }

        val transformsByTarget = explicitTransforms.associateBy { it.target }
        fun nestedClassRenameTransforms(descriptor: ClassDescriptor): Collection<ResolvedApiTransform> {
            return descriptor.unsubstitutedMemberScope.getContributedDescriptors().filterIsInstance<ClassDescriptor>()
                .filter { it.kind == ClassKind.CLASS || it.kind == ClassKind.OBJECT }
                .flatMap { childDescriptor ->
                    val target = ResolvedApiTransform.Target.Type(childDescriptor)
                    val existingTransform = transformsByTarget[target] as? ResolvedApiTransform.TypeTransform
                    val childTransform = if (existingTransform != null) {
                        if (existingTransform.newSwiftName != null) {
                            // Already renamed.
                            null
                        } else {
                            existingTransform.copy(
                                newSwiftName = resolveName(childDescriptor)
                            )
                        }
                    } else {
                        ResolvedApiTransform.TypeTransform(
                            target = target,
                            isProtocol = childDescriptor.kind == ClassKind.INTERFACE,
                            classOrProtocolName = namer.getClassOrProtocolName(childDescriptor),
                            newSwiftName = resolveName(childDescriptor),
                            bridgedName = null,
                            hide = false,
                            remove = false,
                        )
                    }

                    listOfNotNull(childTransform) + nestedClassRenameTransforms(childDescriptor)
                }
        }

        val nestedClassRenameTransforms = explicitTransforms.filterIsInstance<ResolvedApiTransform.TypeTransform>()
            .filter { it.newSwiftName != null }
            .flatMap {
                nestedClassRenameTransforms(it.target.descriptor)
            }

        explicitTransforms + containerTransforms + nestedClassRenameTransforms
    }

    val transformsByTarget: Map<ResolvedApiTransform.Target, ResolvedApiTransform> by lazy {
        resolvedTransforms.associateBy { it.target }
    }

    val typeTransforms by lazy {
        resolvedTransforms.filterIsInstance<ResolvedApiTransform.TypeTransform>()
    }

    val classTransforms by lazy {
        typeTransforms.filter { !it.isProtocol }
    }

    val fileTransforms by lazy {
        resolvedTransforms.filterIsInstance<ResolvedApiTransform.FileTransform>()
    }

    val protocolTransforms by lazy {
        typeTransforms.filter { it.isProtocol }
    }

    private val propertyMap by lazy {
        resolvedTransforms.filterIsInstance<ResolvedApiTransform.PropertyTransform>().groupBy { it.target.descriptor.parent }
    }

    private val functionMap by lazy {
        resolvedTransforms.filterIsInstance<ResolvedApiTransform.FunctionTransform>().groupBy { it.target.descriptor.parent }
    }

    fun findPropertyTransform(descriptor: PropertyDescriptor): ResolvedApiTransform.PropertyTransform? {
        return transformsByTarget[ResolvedApiTransform.Target.Property(descriptor)] as? ResolvedApiTransform.PropertyTransform?
    }

    fun findFunctionTransform(descriptor: FunctionDescriptor): ResolvedApiTransform.FunctionTransform? {
        return transformsByTarget[ResolvedApiTransform.Target.Function(descriptor)] as? ResolvedApiTransform.FunctionTransform?
    }

    fun findTypeTransform(descriptor: ClassDescriptor): ResolvedApiTransform.TypeTransform? {
        return transformsByTarget[ResolvedApiTransform.Target.Type(descriptor)] as? ResolvedApiTransform.TypeTransform?
    }

    fun findFileClassTransform(target: ResolvedApiTransform.Target.File): ResolvedApiTransform.FileTransform? {
        return transformsByTarget[target] as? ResolvedApiTransform.FileTransform?
    }

    fun findPropertyTransformsOfParent(parent: ResolvedApiTransform.Target.CallableMemberParent): List<ResolvedApiTransform.PropertyTransform> {
        return propertyMap[parent] ?: emptyList()
    }

    fun findFunctionTransformsOfParent(parent: ResolvedApiTransform.Target.CallableMemberParent): List<ResolvedApiTransform.FunctionTransform> {
        return functionMap[parent] ?: emptyList()
    }
}
