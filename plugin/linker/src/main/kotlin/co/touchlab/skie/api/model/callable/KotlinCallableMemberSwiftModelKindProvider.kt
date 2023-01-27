package co.touchlab.skie.api.model.callable

import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.types.typeUtil.isEnum
import org.jetbrains.kotlin.types.typeUtil.isInterface

val CallableMemberDescriptor.swiftModelKind: KotlinCallableMemberSwiftModel.Kind
    get() = when {
        dispatchReceiverParameter == null && extensionReceiverParameter == null -> KotlinCallableMemberSwiftModel.Kind.Global
        dispatchReceiverParameter != null -> {
            if (this.dispatchReceiverParameter?.type?.isInterface() == true) {
                KotlinCallableMemberSwiftModel.Kind.Member.Interface
            } else if (this.dispatchReceiverParameter?.type?.isEnum() == true) {
                KotlinCallableMemberSwiftModel.Kind.Member.Enum
            } else {
                KotlinCallableMemberSwiftModel.Kind.Member.Class
            }
        }
        extensionReceiverParameter != null -> {
            if (this.extensionReceiverParameter?.type?.isInterface() == true) {
                KotlinCallableMemberSwiftModel.Kind.Extension.Interface
            } else if (this.extensionReceiverParameter?.type?.isEnum() == true) {
                KotlinCallableMemberSwiftModel.Kind.Extension.Enum
            } else {
                KotlinCallableMemberSwiftModel.Kind.Extension.Class
            }
        }
        else -> error("All cases should be covered.")
    }
