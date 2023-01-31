package co.touchlab.skie.plugin.api.model.callable

import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor

interface KotlinCallableMemberSwiftModel {

    val descriptor: CallableMemberDescriptor

    val original: KotlinCallableMemberSwiftModel

    val receiver: TypeSwiftModel

    val kind: Kind

    val scope: Scope
        get() = if (kind in listOf(Kind.Global, Kind.Extension.Interface)) Scope.Static else Scope.Member

    val allBoundedSwiftModels: List<KotlinCallableMemberSwiftModel>

    val directlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>

    fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT

    sealed interface Kind {

        object Global : Kind

        sealed interface Member : Kind {

            object Class : Member

            object Enum : Member, FromEnum

            object Interface : Member
        }

        sealed interface Extension : Kind {

            object Class : Extension

            object Enum : Extension, FromEnum

            object Interface : Extension
        }

        sealed interface FromEnum : Kind
    }

    enum class Scope {
        Static, Member
    }
}
