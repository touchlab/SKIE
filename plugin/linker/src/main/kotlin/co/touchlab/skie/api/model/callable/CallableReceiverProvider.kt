@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.callable

// context(MutableSwiftModelScope)
// private fun CallableMemberDescriptor.getReceiverSwiftModel(namer: ObjCExportNamer): MutableKotlinTypeSwiftModel {
//     val categoryClass = namer.mapper.getClassIfCategory(this)
//     val containingDeclaration = this.containingDeclaration
//
//     return when {
//         categoryClass != null -> categoryClass.swiftModel
//         this is PropertyAccessorDescriptor -> correspondingProperty.swiftModel.receiver
//         containingDeclaration is ClassDescriptor -> containingDeclaration.swiftModel
//         containingDeclaration is PackageFragmentDescriptor -> this.findSourceFile().swiftModel
//         else -> error("Unsupported containing declaration for $this")
//     }
// }
