package co.touchlab.skie.plugin.api.model.callable.property

import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

interface KotlinPropertySwiftModel : KotlinCallableMemberSwiftModel {

    override val descriptor: PropertyDescriptor

    val type: TypeSwiftModel
}
