package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spec.CallableMemberReference
import co.touchlab.swiftpack.spec.KotlinPackageReference
import co.touchlab.swiftpack.spec.KotlinTypeReference
import co.touchlab.swiftpack.spec.SwiftPackReference
import co.touchlab.swiftpack.spi.SwiftNameProvider
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor

class ObjCExportNamerSwiftNameProvider(
    private val namer: ObjCExportNamer,
    private val kotlinNameResolver: KotlinNameResolver,
    private val transformResolver: TransformResolver,
    private val referenceResolver: SwiftReferenceResolver,
): SwiftNameProvider {

    override fun getSwiftTypeName(kotlinTypeReference: SwiftPackReference): String {
        return referenceResolver.resolveTypeReference(kotlinTypeReference).let { reference ->
            transformResolver.findTypeTransform(reference)?.newSwiftName ?: namer.getClassOrProtocolName(kotlinNameResolver.resolveClass(reference)).swiftName
        }
    }

    override fun getSwiftPropertyName(kotlinPropertyReference: SwiftPackReference): String {
        return referenceResolver.resolvePropertyReference(kotlinPropertyReference).let { reference ->
            val propertyDescriptor = lazy { kotlinNameResolver.resolveProperty(reference) }
            val parentPrefix = getParentPrefix(reference, propertyDescriptor)
            val propertyName = transformResolver.findPropertyTransform(reference)?.newSwiftName ?: namer.getPropertyName(propertyDescriptor.value)
            parentPrefix + propertyName
        }
    }

    override fun getSwiftFunctionSelector(kotlinFunctionReference: SwiftPackReference): String {
        return referenceResolver.resolveFunctionReference(kotlinFunctionReference).let { reference ->
            val functionDescriptor = lazy { kotlinNameResolver.resolveFunction(reference) }
            val parentPrefix = getParentPrefix(reference, functionDescriptor)
            val functionSelector = transformResolver.findFunctionTransform(reference)?.newSwiftSelector
                ?: namer.getSwiftName(functionDescriptor.value)
            val swiftFunctionReference = if (reference.parameterTypes.isEmpty()) {
                functionSelector.dropLast(2)
            } else {
                functionSelector
            }
            parentPrefix + swiftFunctionReference
        }
    }

    private fun getParentPrefix(reference: CallableMemberReference, descriptor: Lazy<CallableMemberDescriptor>): String {
        return when (reference.parent) {
            is KotlinPackageReference -> descriptor.value.findSourceFileInPackage().let { file ->
                transformResolver.findFileTransform(file)?.newSwiftName ?: namer.getFileClassName(file.file).swiftName
            } + "."
            is KotlinTypeReference -> ""
        }
    }
}
