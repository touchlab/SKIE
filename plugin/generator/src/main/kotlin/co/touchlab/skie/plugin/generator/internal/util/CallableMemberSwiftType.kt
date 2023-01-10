package co.touchlab.skie.plugin.generator.internal.util

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.types.typeUtil.isEnum
import org.jetbrains.kotlin.types.typeUtil.isInterface

sealed interface CallableMemberSwiftType {

    object Function : CallableMemberSwiftType

    sealed interface Method : CallableMemberSwiftType {

        object Class : Method

        object Enum : Method, FromEnum

        object Interface : Method
    }

    sealed interface Extension : CallableMemberSwiftType {

        object Class : Extension

        object Enum : Extension, FromEnum

        object Interface : Extension
    }

    sealed interface FromEnum : CallableMemberSwiftType
}

val CallableMemberDescriptor.swiftKind: CallableMemberSwiftType
    get() = when {
        dispatchReceiverParameter == null && extensionReceiverParameter == null -> CallableMemberSwiftType.Function
        dispatchReceiverParameter != null -> {
            if (this.dispatchReceiverParameter?.type?.isInterface() == true) {
                CallableMemberSwiftType.Method.Interface
            } else if (this.dispatchReceiverParameter?.type?.isEnum() == true) {
                CallableMemberSwiftType.Method.Enum
            } else {
                CallableMemberSwiftType.Method.Class
            }
        }
        extensionReceiverParameter != null -> {
            if (this.extensionReceiverParameter?.type?.isInterface() == true) {
                CallableMemberSwiftType.Extension.Interface
            } else if (this.extensionReceiverParameter?.type?.isEnum() == true) {
                CallableMemberSwiftType.Extension.Enum
            } else {
                CallableMemberSwiftType.Extension.Class
            }
        }
        else -> error("All cases should be covered.")
    }
