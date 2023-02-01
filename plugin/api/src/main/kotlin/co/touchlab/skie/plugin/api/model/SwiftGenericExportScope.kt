package co.touchlab.skie.plugin.api.model

import co.touchlab.skie.plugin.api.model.type.translation.SwiftGenericTypeUsageModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface

interface SwiftGenericExportScope {

    fun getGenericTypeUsage(typeParameterDescriptor: TypeParameterDescriptor?): SwiftGenericTypeUsageModel?

    class Class(container: DeclarationDescriptor, val namer: ObjCExportNamer) : SwiftGenericExportScope {

        private val typeNames = if (container is ClassDescriptor && !container.kind.isInterface) {
            container.typeConstructor.parameters
        } else {
            emptyList<TypeParameterDescriptor>()
        }

        override fun getGenericTypeUsage(typeParameterDescriptor: TypeParameterDescriptor?): SwiftGenericTypeUsageModel? {
            val localTypeParam = typeNames.firstOrNull {
                typeParameterDescriptor != null &&
                    (it == typeParameterDescriptor || (it.isCapturedFromOuterDeclaration && it.original == typeParameterDescriptor))
            }

            return if (localTypeParam == null) {
                null
            } else {
                SwiftGenericTypeUsageModel(localTypeParam, namer)
            }
        }
    }

    object None : SwiftGenericExportScope {

        override fun getGenericTypeUsage(typeParameterDescriptor: TypeParameterDescriptor?): SwiftGenericTypeUsageModel? = null
    }
}

