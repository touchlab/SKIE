package co.touchlab.skie.swiftmodel.callable.property

import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModel
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

interface KotlinPropertySwiftModel : KotlinCallableMemberSwiftModel {

    override val descriptor: PropertyDescriptor
}
