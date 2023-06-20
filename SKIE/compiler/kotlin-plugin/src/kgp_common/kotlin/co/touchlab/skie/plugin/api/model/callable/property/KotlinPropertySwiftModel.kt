package co.touchlab.skie.plugin.api.model.callable.property

import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

interface KotlinPropertySwiftModel : KotlinCallableMemberSwiftModel {

    override val descriptor: PropertyDescriptor
}
