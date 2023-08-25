package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.sir.SwiftFqName
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrExtensibleDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrModule
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrProtocolDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration
import org.jetbrains.kotlin.backend.konan.isExternalObjCClass
import org.jetbrains.kotlin.backend.konan.isObjCForwardDeclaration
import org.jetbrains.kotlin.backend.konan.isObjCMetaClass
import org.jetbrains.kotlin.backend.konan.isObjCProtocolClass
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassOrAny
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

class SwiftIrDeclarationRegistry(
    private val namer: ObjCExportNamer,
) {

    val builtinKotlinDeclarations: BuiltinDeclarations.Kotlin = BuiltinDeclarations.Kotlin(namer)

    private val declarationsByDescriptor = mutableMapOf<ClassDescriptor, SwiftIrDeclaration>()

    private val modules = mutableMapOf<String, SwiftIrModule>()
    private val externalTypeDeclarations = mutableMapOf<SwiftFqName.External, SwiftIrTypeDeclaration.External>()
    private val externalProtocolDeclarations = mutableMapOf<SwiftFqName.External, SwiftIrProtocolDeclaration.External>()

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

    fun referenceExternalTypeDeclaration(
        fqName: SwiftFqName.External,
        defaultSupertypes: List<SwiftIrTypeDeclaration> = emptyList(),
    ): SwiftIrTypeDeclaration.External {
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

    fun referenceExternalProtocolDeclaration(
        fqName: SwiftFqName.External.TopLevel,
        defaultSupertypes: List<SwiftIrExtensibleDeclaration> = emptyList(),
    ): SwiftIrProtocolDeclaration {
        return externalProtocolDeclarations.getOrPut(fqName) {
            SwiftIrProtocolDeclaration.External(
                module = referenceModule(fqName.module),
                name = fqName.name,
                superTypes = defaultSupertypes,
            )
        }
    }

    context(SwiftModelScope)
    fun declarationForClass(descriptor: ClassDescriptor): SwiftIrExtensibleDeclaration =
        resolveDeclaration(descriptor) as SwiftIrExtensibleDeclaration? ?: error("No declaration for $descriptor")

    context(SwiftModelScope)
    fun declarationForInterface(descriptor: ClassDescriptor): SwiftIrProtocolDeclaration =
        resolveDeclaration(descriptor) as SwiftIrProtocolDeclaration? ?: error("No declaration for $descriptor")

    context(SwiftModelScope)
    fun declarationForSwiftModel(swiftModel: KotlinClassSwiftModel): SwiftIrExtensibleDeclaration.Local =
        declarationsByDescriptor.getOrPut(swiftModel.classDescriptor) {
            val superTypesDeclarations = (swiftModel.classDescriptor.getSuperInterfaces() + swiftModel.classDescriptor.getSuperClassOrAny())
                .mapNotNull { resolveDeclaration(it) as SwiftIrExtensibleDeclaration? }

            if (swiftModel.kind.isInterface) {
                SwiftIrProtocolDeclaration.Local.KotlinInterface.Modeled(swiftModel, superTypesDeclarations)
            } else {
                SwiftIrTypeDeclaration.Local.KotlinClass.Modeled(swiftModel, superTypesDeclarations)
            }
        } as SwiftIrExtensibleDeclaration.Local

    context(SwiftModelScope)
    private fun resolveDeclaration(descriptor: ClassDescriptor): SwiftIrDeclaration? = when {
        descriptor.hasSwiftModel -> descriptor.swiftModel.bridge?.declaration ?: declarationForSwiftModel(descriptor.swiftModel)
        descriptor.isObjCMetaClass() -> BuiltinDeclarations.AnyClass
        descriptor.isObjCProtocolClass() -> BuiltinDeclarations.Protocol
        descriptor.isExternalObjCClass() || descriptor.isObjCForwardDeclaration() -> {
            val moduleName = descriptor.fqNameSafe.pathSegments()[1].asString()
            if (descriptor.kind.isInterface) {
                val fqName = SwiftFqName.External.TopLevel(moduleName, descriptor.name.asString().removeSuffix("Protocol"))
                referenceExternalProtocolDeclaration(fqName, listOf(BuiltinDeclarations.Foundation.NSObject))
            } else {
                val fqName = SwiftFqName.External.TopLevel(moduleName, descriptor.name.asString())
                referenceExternalTypeDeclaration(fqName, listOf(BuiltinDeclarations.Foundation.NSObject))
            }
        }
        descriptor == descriptor.builtIns.any -> builtinKotlinDeclarations.Base
        else -> null
    }
}
