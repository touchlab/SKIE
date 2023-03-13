package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.SwiftFqName
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrExtensibleDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrModule
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrProtocolDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeParameterDeclaration
import org.jetbrains.kotlin.backend.konan.isExternalObjCClass
import org.jetbrains.kotlin.backend.konan.isObjCForwardDeclaration
import org.jetbrains.kotlin.backend.konan.isObjCMetaClass
import org.jetbrains.kotlin.backend.konan.isObjCProtocolClass
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module

class SwiftIrDeclarationRegistry(
    private val namer: ObjCExportNamer,
) {
    private val declarations = mutableSetOf<SwiftIrDeclaration>()
    private val declarationsByDescriptor = mutableMapOf<ClassDescriptor, SwiftIrDeclaration>()

    private val modules = mutableMapOf<String, SwiftIrModule>()
    private val externalTypeDeclarations = mutableMapOf<SwiftFqName.External, SwiftIrTypeDeclaration.External>()

    init {
        val knownExternalDeclarations = listOf(
            BuiltinDeclarations.Swift.Array,
            BuiltinDeclarations.Swift.Dictionary,
            BuiltinDeclarations.Swift.Set,
        )

        knownExternalDeclarations.forEach {
            externalTypeDeclarations[it.publicName] = it
        }
    }

    fun referenceModule(name: String): SwiftIrModule {
        return modules.getOrPut(name) {
            SwiftIrModule(name)
        }
    }

    fun referenceExternalTypeDeclaration(fqName: SwiftFqName.External, defaultSupertypes: List<SwiftIrTypeDeclaration> = emptyList()): SwiftIrTypeDeclaration.External {
        // TODO: Check if `defaultSupertypes` equal to the existing declaration's supertypes.
        return externalTypeDeclarations.getOrPut(fqName) {
            when (fqName) {
                is SwiftFqName.External.Nested -> {
                    // We don't pass the default supertypes here, because it might be nested in a whole different unknown type.
                    val containingDeclaration = referenceExternalTypeDeclaration(fqName.parent)
                    SwiftIrTypeDeclaration.External(
                        module = containingDeclaration.module,
                        name = fqName.name,
                        containingDeclaration = containingDeclaration,
                        superTypes = defaultSupertypes,
                    )
                }
                is SwiftFqName.External.TopLevel -> SwiftIrTypeDeclaration.External(
                    module = referenceModule(fqName.module),
                    name = fqName.name,
                    superTypes = defaultSupertypes,
                )
            }

        }
    }

    context(SwiftModelScope)
    fun declarationForClass(descriptor: ClassDescriptor): SwiftIrExtensibleDeclaration = declarationsByDescriptor.getOrPut(descriptor) {
        resolveDeclaration(descriptor)
    } as SwiftIrExtensibleDeclaration

    context(SwiftModelScope)
    fun declarationForInterface(descriptor: ClassDescriptor): SwiftIrProtocolDeclaration = declarationsByDescriptor.getOrPut(descriptor) {
        resolveDeclaration(descriptor)
    } as SwiftIrProtocolDeclaration

    context(SwiftModelScope)
    private fun resolveDeclaration(descriptor: ClassDescriptor): SwiftIrDeclaration = when {
        // TODO: Remove these special cases, handle just the default one.
        descriptor.hasSwiftModel -> descriptor.swiftModel.swiftIrDeclaration
        descriptor.isObjCMetaClass() -> BuiltinDeclarations.AnyClass
        descriptor.isObjCProtocolClass() -> BuiltinDeclarations.Protocol
        descriptor.isExternalObjCClass() || descriptor.isObjCForwardDeclaration() -> {
            val module = referenceModule(descriptor.fqNameSafe.pathSegments()[1].asString())
            if (descriptor.kind.isInterface) {
                SwiftIrProtocolDeclaration.External(
                    module = module,
                    name = descriptor.name.asString().removeSuffix("Protocol"),
                    superTypes = listOf(BuiltinDeclarations.Foundation.NSObject),
                )
            } else {
                SwiftIrTypeDeclaration.External(
                    module = module,
                    name = descriptor.name.asString(),
                    superTypes = listOf(BuiltinDeclarations.Foundation.NSObject),
                )
            }
        }

        else -> declarationsByDescriptor.getOrPut(descriptor) {
            SwiftIrTypeDeclaration.Local.KotlinClass.Immutable(
                kotlinModule = descriptor.module.name.asString(),
                kotlinFqName = descriptor.fqNameSafe,
                swiftName = namer.getClassOrProtocolName(descriptor).swiftName,
                typeParameters = descriptor.declaredTypeParameters.map {
                    SwiftIrTypeParameterDeclaration.KotlinTypeParameter(
                        descriptor = it,
                        name = namer.getTypeParameterName(it),
                        bounds = it.upperBounds.map { bound ->
                            bound.constructor.declarationDescriptor?.let {
                                declarationForClass(it as ClassDescriptor)
                            } ?: TODO()
                        }
                    )
                },
            )
        }
    }
}
