package co.touchlab.skie.plugin.api.model.callable.property.converted

import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.KotlinPropertySwiftModel

interface KotlinConvertedPropertySwiftModel : KotlinPropertySwiftModel {

    override val directlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>
        get() = accessors

    val getter: KotlinFunctionSwiftModel

    val setter: KotlinFunctionSwiftModel?

    val accessors: List<KotlinFunctionSwiftModel>
        get() = listOfNotNull(getter, setter)
}
