package co.touchlab.skie.api.model.callable

import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.types.typeUtil.isEnum
import org.jetbrains.kotlin.types.typeUtil.isInterface

val CallableMemberDescriptor.swiftModelOrigin: KotlinCallableMemberSwiftModel.Origin
    get() = when {
        dispatchReceiverParameter == null && extensionReceiverParameter == null -> KotlinCallableMemberSwiftModel.Origin.Global
        dispatchReceiverParameter != null -> {
            if (this.dispatchReceiverParameter?.type?.isInterface() == true) {
                KotlinCallableMemberSwiftModel.Origin.Member.Interface
            } else if (this.dispatchReceiverParameter?.type?.isEnum() == true) {
                KotlinCallableMemberSwiftModel.Origin.Member.Enum
            } else {
                KotlinCallableMemberSwiftModel.Origin.Member.Class
            }
        }
        extensionReceiverParameter != null -> {
            if (this.extensionReceiverParameter?.type?.isInterface() == true) {
                KotlinCallableMemberSwiftModel.Origin.Extension.Interface
            } else if (this.extensionReceiverParameter?.type?.isEnum() == true) {
                KotlinCallableMemberSwiftModel.Origin.Extension.Enum
            } else {
                KotlinCallableMemberSwiftModel.Origin.Extension.Class
            }
        }
        else -> error("All cases should be covered.")
    }
