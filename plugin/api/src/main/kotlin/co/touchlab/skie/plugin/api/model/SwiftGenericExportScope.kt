package co.touchlab.skie.plugin.api.model

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.model.type.translation.SwiftGenericTypeUsageSirType
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeParameterDeclaration
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface

interface SwiftGenericExportScope {

    fun getGenericTypeUsage(typeParameterDescriptor: TypeParameterDescriptor?): SwiftGenericTypeUsageSirType?

    class FromTypeDeclaration(
        val declaration: SwiftIrTypeDeclaration,
    ): SwiftGenericExportScope {
        override fun getGenericTypeUsage(typeParameterDescriptor: TypeParameterDescriptor?): SwiftGenericTypeUsageSirType? {
            val localTypeParam = declaration.typeParameters.firstOrNull {
                // TODO: We don't really want to cast, but here we're only resolving Kotlin type params anyway
                typeParameterDescriptor != null && ((it as? SwiftIrTypeParameterDeclaration.KotlinTypeParameter)?.descriptor == typeParameterDescriptor /* || (it.isCapturedFromOuterDeclaration && it.original == typeParameterDescriptor) */)
            }

            return localTypeParam?.let {
                SwiftGenericTypeUsageSirType(localTypeParam)
            }
        }
    }

    class Class(container: DeclarationDescriptor, val namer: ObjCExportNamer) : SwiftGenericExportScope {

        private val typeNames = if (container is ClassDescriptor && !container.kind.isInterface) {
            container.typeConstructor.parameters
        } else {
            emptyList<TypeParameterDescriptor>()
        }

        override fun getGenericTypeUsage(typeParameterDescriptor: TypeParameterDescriptor?): SwiftGenericTypeUsageSirType? {
            val localTypeParam = typeNames.firstOrNull {
                typeParameterDescriptor != null &&
                    (it == typeParameterDescriptor || (it.isCapturedFromOuterDeclaration && it.original == typeParameterDescriptor))
            }

            return if (localTypeParam == null) {
                null
            } else {
                // TODO: The TypeParameterDeclaration should be a singleton, but it isn't here. This whole implementation should go away.
                SwiftGenericTypeUsageSirType(
                    SwiftIrTypeParameterDeclaration.KotlinTypeParameter(
                        name = namer.getTypeParameterName(localTypeParam),
                        bounds = listOf(BuiltinDeclarations.Swift.AnyObject),
                        descriptor = localTypeParam,
                    )
                )
            }
        }
    }

    object None : SwiftGenericExportScope {

        override fun getGenericTypeUsage(typeParameterDescriptor: TypeParameterDescriptor?): SwiftGenericTypeUsageSirType? = null
    }
}
