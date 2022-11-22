package co.touchlab.skie.plugin

import co.touchlab.skie.api.impl.DefaultMutableSwiftFunctionName
import co.touchlab.skie.api.impl.DefaultMutableSwiftParameterName
import co.touchlab.skie.api.impl.DefaultMutableSwiftTypeName
import co.touchlab.skie.plugin.api.function.MutableSwiftFunctionName
import co.touchlab.skie.plugin.api.type.MutableSwiftTypeName
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor

internal class NameResolver(
    private val namer: ObjCExportNamer,
) {
    private val mutableTypeNames = mutableMapOf<TransformAccumulator.TypeTransformTarget, MutableSwiftTypeName>()
    private val mutableFunctionNames = mutableMapOf<FunctionDescriptor, MutableSwiftFunctionName>()

    val typeNames: Map<TransformAccumulator.TypeTransformTarget, MutableSwiftTypeName> = mutableTypeNames
    val functionNames: Map<FunctionDescriptor, MutableSwiftFunctionName> = mutableFunctionNames

    fun resolveName(target: TransformAccumulator.TypeTransformTarget): MutableSwiftTypeName = mutableTypeNames.getOrPut(target) {
        when (target) {
            is TransformAccumulator.TypeTransformTarget.Class -> resolveClassName(target)
            is TransformAccumulator.TypeTransformTarget.File -> resolveFileName(target)
        }
    }

    fun resolveName(functionDescriptor: FunctionDescriptor): MutableSwiftFunctionName = mutableFunctionNames.getOrPut(functionDescriptor) {
        val swiftName = namer.getSwiftName(functionDescriptor)
        val (functionName, parametersString) = "(.+?)\\((.*?)\\)".toRegex().matchEntire(swiftName)?.destructured ?: error("Unable to parse swift name: $swiftName")
        val parameters = parametersString.split(":").map { it.trim() }.filter { it.isNotEmpty() }

        DefaultMutableSwiftFunctionName(
            functionName,
            parameters.map { DefaultMutableSwiftParameterName(it) },
        )
    }

    private fun resolveFileName(target: TransformAccumulator.TypeTransformTarget.File): MutableSwiftTypeName = DefaultMutableSwiftTypeName(
        originalParent = null,
        originalSimpleName = namer.getFileClassName(target.file).swiftName,
    )

    private fun resolveClassName(target: TransformAccumulator.TypeTransformTarget.Class): MutableSwiftTypeName {
        val isEnumEntry = target.descriptor.kind == ClassKind.ENUM_ENTRY
        val name = if (isEnumEntry) {
            namer.getEnumEntrySelector(target.descriptor)
        } else {
            namer.getClassOrProtocolName(target.descriptor).swiftName
        }
        return when (val parent = target.descriptor.containingDeclaration) {
            is PackageFragmentDescriptor, is PackageViewDescriptor -> DefaultMutableSwiftTypeName(
                originalParent = null,
                originalSimpleName = name,
            )
            is ClassDescriptor -> {
                val parentName = resolveName(TransformAccumulator.TypeTransformTarget.Class(parent))
                val parentQualifiedName = parentName.originalQualifiedName
                val simpleNameCandidate = if (name.startsWith(parentQualifiedName)) {
                    name.drop(parentQualifiedName.length)
                } else {
                    name
                }
                val (realParent, simpleName) = if (simpleNameCandidate.startsWith('.')) {
                    parentName to simpleNameCandidate.drop(1)
                } else if (isEnumEntry) {
                    parentName to simpleNameCandidate
                } else {
                    parentName.parent to parentName.originalSimpleName + simpleNameCandidate
                }
                DefaultMutableSwiftTypeName(
                    originalParent = realParent,
                    originalSimpleName = simpleName,
                )
            }
            else -> error("Unexpected parent type: $parent")
        }
    }
}
