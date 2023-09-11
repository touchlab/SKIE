package co.touchlab.skie.swiftmodel.callable

import co.touchlab.skie.swiftmodel.SwiftGenericExportScope
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import co.touchlab.skie.sir.type.SirType
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor

interface KotlinCallableMemberSwiftModel {

    val descriptor: CallableMemberDescriptor

    // TODO hack to avoid problems with some special Kotlin classes not having a SwiftModel yet, safe to use with !! for suspend functions
    val owner: KotlinTypeSwiftModel?

    val receiver: SirType

    val origin: Origin

    val scope: Scope

    val allBoundedSwiftModels: List<KotlinCallableMemberSwiftModel>

    val directlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>

    fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT

    // TODO Remove? - probably not needed anymore
    sealed interface Origin {

        sealed interface FromEnum : Origin

        object Global : Origin

        sealed interface Member : Origin {

            object Class : Member

            object Enum : Member, FromEnum

            object Interface : Member
        }

        sealed interface Extension : Origin {

            object Class : Extension

            object Enum : Extension, FromEnum

            object Interface : Extension
        }
    }

    enum class Scope {
        Static, Member
    }
}

val KotlinCallableMemberSwiftModel.Scope.isStatic: Boolean
    get() = this == KotlinCallableMemberSwiftModel.Scope.Static

val KotlinCallableMemberSwiftModel.Scope.isMember: Boolean
    get() = this == KotlinCallableMemberSwiftModel.Scope.Member

val KotlinCallableMemberSwiftModel.swiftGenericExportScope: SwiftGenericExportScope
    get() = owner?.swiftGenericExportScope ?: SwiftGenericExportScope.None
