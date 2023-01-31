package co.touchlab.skie.plugin.api.model

import co.touchlab.skie.plugin.api.model.type.translation.SwiftGenericTypeParameterUsageModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftGenericTypeUsageModel
import co.touchlab.skie.plugin.api.util.isInterface
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor

interface SwiftGenericExportScope {

    fun getGenericTypeUsage(typeParameterDescriptor: TypeParameterDescriptor?): SwiftGenericTypeUsageModel?

    class Class(container: DeclarationDescriptor, val namer: ObjCExportNamer) : SwiftGenericExportScope {

        private val typeNames = if (container is ClassDescriptor && !container.isInterface) {
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
                SwiftGenericTypeParameterUsageModel(localTypeParam, namer)
            }
        }
    }

    object None : SwiftGenericExportScope {

        override fun getGenericTypeUsage(typeParameterDescriptor: TypeParameterDescriptor?): SwiftGenericTypeUsageModel? = null
    }
}

data class SwiftExportScope(
    val genericScope: SwiftGenericExportScope,
    val flags: Set<Flags>,
) {

    constructor(
        genericScope: SwiftGenericExportScope,
        vararg flags: Flags,
    ) : this(genericScope, flags.toSet())

    fun replacingFlags(vararg flags: Flags): SwiftExportScope = SwiftExportScope(genericScope, flags.toSet())

    fun addingFlags(vararg flags: Flags): SwiftExportScope = SwiftExportScope(genericScope, flags.toSet() + this.flags)

    fun removingFlags(vararg flags: Flags): SwiftExportScope = SwiftExportScope(genericScope, this.flags - flags.toSet())

    fun hasFlag(flag: Flags): Boolean = flags.contains(flag)

    fun hasAllFlags(vararg flags: Flags) = flags.all(::hasFlag)

    enum class Flags {
        Escaping,
        ReferenceType,
        Hashable,
    }
}
